def i(n, a, u):
    return {"name": n, "amount": a, "unit": u, "food_id": None}

def r(id_num, name, en, cu, cat, sc, df, pp, ck, sv, cal, tags, ingr, steps, tips, nt, sf, desc):
    return {
        "id": f"recipe_{id_num:03d}",
        "name": name, "name_en": en, "cuisine": cu,
        "category": cat, "sub_category": sc, "difficulty": df,
        "prep_time_minutes": pp, "cook_time_minutes": ck,
        "servings": sv, "calories_per_serving": cal, "tags": tags,
        "ingredients": ingr, "steps": steps, "tips": tips,
        "nutrition_summary": {"protein_g": nt[0], "fat_g": nt[1], "carb_g": nt[2], "fiber_g": nt[3]},
        "suitable_for": sf, "description": desc
    }
