import os
import re

directory = r'c:\Users\phant\BTL_NMCNPM\BE\backend\src\main\webapp'

for filename in os.listdir(directory):
    if filename.endswith('.jsp'):
        path = os.path.join(directory, filename)
        with open(path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Add isELIgnored="true" to the first <%@ page ... %> if not present
        if 'isELIgnored="true"' not in content:
            content = re.sub(r'<%@\s*page', '<%@ page isELIgnored="true"', content, count=1)
        
        # Remove backslashes before ${
        content = content.replace(r'\${', '${')
        
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)

print("Done processing JSPs.")
