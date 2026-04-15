#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
NutriVista 菜谱数据质量验证脚本
用法: python validate_recipes.py [path_to_json]
"""

import json
import sys
from collections import Counter

VALID_CUISINES = {
    "川菜", "鲁菜", "粤菜", "苏菜", "浙菜", "闽菜", "湘菜", "徽菜",
    "家常菜", "凉菜", "汤羹", "面食/主食", "小吃点心", "早餐",
    "火锅", "烧烤", "素菜", "药膳"
}

REQUIRED_FIELDS = [
    "id", "name", "name_en", "cuisine", "category", "sub_category",
    "difficulty", "prep_time_minutes", "cook_time_minutes", "servings",
    "calories_per_serving", "tags", "ingredients", "steps", "tips",
    "nutrition_summary", "suitable_for", "description"
]

VALID_DIFFICULTIES = {"easy", "medium", "hard"}

NUTRITION_FIELDS = ["protein_g", "fat_g", "carb_g", "fiber_g"]


def validate(path: str) -> None:
    print(f"正在读取: {path}")
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)

    recipes = data if isinstance(data, list) else data.get("recipes", [])
    print(f"共 {len(recipes)} 道菜谱\n")

    errors = []
    warnings = []
    ids_seen = Counter()

    for i, r in enumerate(recipes):
        loc = f"[#{i+1} {r.get('id','?')} {r.get('name','?')}]"

        # 1. 必填字段非空
        for field in REQUIRED_FIELDS:
            val = r.get(field)
            if val is None or val == "" or val == [] or val == {}:
                errors.append(f"{loc} 缺少必填字段: {field}")

        # 2. id 唯一性
        rid = r.get("id", "")
        ids_seen[rid] += 1

        # 3. cuisine 合法性
        cuisine = r.get("cuisine", "")
        if cuisine and cuisine not in VALID_CUISINES:
            errors.append(f"{loc} cuisine 非法值: '{cuisine}'")

        # 4. ingredients 至少 3 项
        ingr = r.get("ingredients", [])
        if isinstance(ingr, list) and len(ingr) < 3:
            errors.append(f"{loc} ingredients 少于3项 (实际:{len(ingr)})")

        # 5. steps 至少 3 步
        steps = r.get("steps", [])
        if isinstance(steps, list) and len(steps) < 3:
            errors.append(f"{loc} steps 少于3步 (实际:{len(steps)})")

        # 6. description 至少 80 字
        desc = r.get("description", "")
        if isinstance(desc, str) and len(desc) < 80:
            errors.append(f"{loc} description 少于80字 (实际:{len(desc)}字)")

        # 7. difficulty 合法性
        diff = r.get("difficulty", "")
        if diff and diff not in VALID_DIFFICULTIES:
            warnings.append(f"{loc} difficulty 非标准值: '{diff}' (建议: easy/medium/hard)")

        # 8. 数值字段非负
        for num_field in ["prep_time_minutes", "cook_time_minutes", "servings", "calories_per_serving"]:
            val = r.get(num_field)
            if val is not None and not isinstance(val, (int, float)):
                errors.append(f"{loc} {num_field} 应为数值")
            elif val is not None and val < 0:
                errors.append(f"{loc} {num_field} 不能为负数")

        # 9. nutrition_summary 字段完整性
        ns = r.get("nutrition_summary", {})
        if isinstance(ns, dict):
            for nf in NUTRITION_FIELDS:
                if nf not in ns:
                    warnings.append(f"{loc} nutrition_summary 缺少字段: {nf}")

        # 10. ingredients 每项有 name 和 amount
        for j, ingr_item in enumerate(r.get("ingredients", [])):
            if not isinstance(ingr_item, dict):
                errors.append(f"{loc} ingredients[{j}] 格式错误")
                continue
            if not ingr_item.get("name"):
                errors.append(f"{loc} ingredients[{j}] 缺少 name")
            if not ingr_item.get("amount"):
                errors.append(f"{loc} ingredients[{j}] 缺少 amount")

    # 重复 id 检查
    for rid, count in ids_seen.items():
        if count > 1:
            errors.append(f"id 重复: '{rid}' 出现 {count} 次")

    # ── 统计输出 ──
    print("=" * 60)
    print(f"菜系/分类分布:")
    cuisine_counter = Counter(r.get("cuisine", "未知") for r in recipes)
    for cuisine, cnt in sorted(cuisine_counter.items(), key=lambda x: -x[1]):
        status = "✓" if cuisine in VALID_CUISINES else "✗"
        print(f"  {status} {cuisine}: {cnt} 道")

    print(f"\n难度分布:")
    diff_counter = Counter(r.get("difficulty", "未知") for r in recipes)
    for d, cnt in diff_counter.items():
        print(f"  {d}: {cnt}")

    print("=" * 60)
    if warnings:
        print(f"\n⚠ 警告 ({len(warnings)} 条):")
        for w in warnings[:20]:
            print(f"  {w}")
        if len(warnings) > 20:
            print(f"  ... (还有 {len(warnings)-20} 条)")

    print()
    if errors:
        print(f"✗ 发现 {len(errors)} 个错误:")
        for e in errors[:30]:
            print(f"  {e}")
        if len(errors) > 30:
            print(f"  ... (还有 {len(errors)-30} 个)")
        print(f"\n验证结果: 失败 (共 {len(errors)} 个错误, {len(warnings)} 个警告)")
        sys.exit(1)
    else:
        print(f"✓ 验证通过！共 {len(recipes)} 道菜谱，{len(warnings)} 个警告")
        avg_desc = sum(len(r.get("description","")) for r in recipes) / max(len(recipes),1)
        avg_ingr = sum(len(r.get("ingredients",[])) for r in recipes) / max(len(recipes),1)
        avg_step = sum(len(r.get("steps",[])) for r in recipes) / max(len(recipes),1)
        print(f"  平均描述长度: {avg_desc:.0f} 字")
        print(f"  平均食材数量: {avg_ingr:.1f} 种")
        print(f"  平均步骤数量: {avg_step:.1f} 步")


if __name__ == "__main__":
    path = sys.argv[1] if len(sys.argv) > 1 else "chinese_recipes.json"
    validate(path)
