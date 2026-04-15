#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
菜谱数据集生成脚本
运行: python generate_recipes.py
输出: chinese_recipes.json
"""
import json
import sys
import os

base = os.path.dirname(os.path.abspath(__file__))

def load_part(filename):
    path = os.path.join(base, filename)
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)

print("正在加载各部分菜谱数据...")
parts = []
for part in ["recipes_part1.json", "recipes_part2.json"]:
    try:
        data = load_part(part)
        print(f"  {part}: {len(data)} 道菜谱")
        parts.extend(data)
    except Exception as e:
        print(f"  !! 加载 {part} 失败: {e}", file=sys.stderr)

print(f"\n合并后共 {len(parts)} 道菜谱")

# 验证 id 唯一性
ids = [r["id"] for r in parts]
if len(ids) != len(set(ids)):
    from collections import Counter
    dup = [k for k, v in Counter(ids).items() if v > 1]
    print(f"警告: 发现重复 id: {dup[:10]}", file=sys.stderr)

out = os.path.join(base, "chinese_recipes.json")
with open(out, "w", encoding="utf-8") as f:
    json.dump(parts, f, ensure_ascii=False, indent=2)

size_kb = os.path.getsize(out) / 1024
print(f"已写入: {out} ({size_kb:.0f} KB)")
print("\n运行验证脚本: python validate_recipes.py chinese_recipes.json")
