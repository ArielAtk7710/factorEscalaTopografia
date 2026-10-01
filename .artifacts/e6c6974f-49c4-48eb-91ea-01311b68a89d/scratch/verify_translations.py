import xml.etree.ElementTree as ET

def get_string_keys(file_path):
    tree = ET.parse(file_path)
    root = tree.getroot()
    keys = set()
    for child in root:
        if child.tag == 'string':
            name = child.get('name')
            if name:
                keys.add(name)
    return keys

es_keys = get_string_keys("app/src/main/res/values/strings.xml")
en_keys = get_string_keys("app/src/main/res/values-en/strings.xml")

print(f"Total ES strings: {len(es_keys)}")
print(f"Total EN strings: {len(en_keys)}")

missing_in_en = es_keys - en_keys
missing_in_es = en_keys - es_keys

if missing_in_en:
    print("\nMissing in EN (values-en/strings.xml):")
    for k in sorted(missing_in_en):
        print(f"  - {k}")

if missing_in_es:
    print("\nMissing in ES (values/strings.xml):")
    for k in sorted(missing_in_es):
        print(f"  - {k}")

if not missing_in_en and not missing_in_es:
    print("\nAll translation keys match perfectly between ES and EN!")
