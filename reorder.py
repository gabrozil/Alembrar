import sys
import os

filepath = 'app/src/main/java/com/clipboardreminder/ui/screen/ReminderListScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# find ReminderItem
ri_idx = -1
for i, line in enumerate(lines):
    if "fun ReminderItem" in line:
        ri_idx = i - 1 # including @Composable
        break

if ri_idx != -1:
    helpers = lines[ri_idx:]
    main_func = lines[:ri_idx]
    
    # find where imports end / where top level starts
    top_idx = -1
    for i, line in enumerate(main_func):
        if "fun ReminderListScreen" in line:
            top_idx = i - 2 # before @OptIn
            break
            
    if top_idx != -1:
        new_lines = main_func[:top_idx] + helpers + main_func[top_idx:]
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.writelines(new_lines)
        print("Reordered successfully!")
    else:
        print("Could not find main func")
else:
    print("Could not find helpers")
