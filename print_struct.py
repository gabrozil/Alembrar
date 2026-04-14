import sys

with open('app/src/main/java/com/clipboardreminder/ui/screen/ReminderListScreen.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

print("File structure:")
for i, line in enumerate(lines):
    if "ReminderItem" in line or "EmptyRemindersPlaceholder" in line or "ReminderDialog" in line or "NotificationIntervalDialog" in line:
        print(f"{i+1}: {line.strip()}")
