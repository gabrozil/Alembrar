import sys

with open('app/src/main/java/com/clipboardreminder/ui/screen/ReminderListScreen.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = lines[:178] + lines[225:]

with open('app/src/main/java/com/clipboardreminder/ui/screen/ReminderListScreen.kt', 'w', encoding='utf-8') as f:
    f.writelines(new_lines)
