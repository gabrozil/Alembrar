import sys

with open('app/src/main/java/com/clipboardreminder/ui/screen/FieldListScreen.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

brace_count = 0
for i, line in enumerate(lines):
    in_string = False
    escape = False
    for char in line:
        if char == '"' and not escape: in_string = not in_string
        escape = (char == '\\' and not escape)
        if not in_string:
            if char == '{': brace_count += 1
            elif char == '}': brace_count -= 1
    if brace_count == 0 and "}" in line:
        print(f"Brace count hit 0 at line {i+1}")

print(f"Final Count: {brace_count}")
