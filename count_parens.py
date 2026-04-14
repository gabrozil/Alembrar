import sys

with open('app/src/main/java/com/clipboardreminder/ui/screen/ReminderListScreen.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

brace_count = 0
paren_count = 0
for i, line in enumerate(lines):
    in_string = False
    escape = False
    for char in line:
        if char == '"' and not escape: in_string = not in_string
        escape = (char == '\\' and not escape)
        if not in_string:
            if char == '{': brace_count += 1
            elif char == '}': brace_count -= 1
            elif char == '(': paren_count += 1
            elif char == ')': paren_count -= 1
    if 275 <= i + 1 <= 285 or paren_count < 0 or brace_count < 0:
        print(f"{i + 1:3}: ({{:{brace_count}, (:{paren_count}) {line.rstrip()}")

print(f"Final Brace Count: {brace_count}")
print(f"Final Paren Count: {paren_count}")
