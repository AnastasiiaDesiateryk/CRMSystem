from openpyxl import Workbook
from pathlib import Path

HEADERS = [
    "Company",
    "Website",
    "LinkedIn",
    "Category NEW",
    "Category OLD",
    "Remark",
    "FLAG",
    "Founding year",
    "Size",
    "Size adjusted",
    "LinkedIn count",
    "City",
    "Cantone",
    "Postal code",
    "Address",
    "Email organization",
    "Name personal contact",
    "Email personal contact (1)",
    "Email personal contact (2)",
    "Solution type",
    "Solution keyword 1",
    "Solution keyword 2",
    "Solution description",
]

def build_file(path: Path, rows: list[dict]) -> None:
    wb = Workbook()
    ws = wb.active
    ws.title = "Database"
    ws.append(HEADERS)

    for row in rows:
        ws.append([
            row.get("Company", ""),
            row.get("Website", ""),
            row.get("LinkedIn", ""),
            row.get("Category NEW", ""),
            row.get("Category OLD", ""),
            row.get("Remark", ""),
            row.get("FLAG", ""),
            row.get("Founding year", ""),
            row.get("Size", ""),
            row.get("Size adjusted", ""),
            row.get("LinkedIn count", ""),
            row.get("City", ""),
            row.get("Cantone", ""),
            row.get("Postal code", ""),
            row.get("Address", ""),
            row.get("Email organization", ""),
            row.get("Name personal contact", ""),
            row.get("Email personal contact (1)", ""),
            row.get("Email personal contact (2)", ""),
            row.get("Solution type", ""),
            row.get("Solution keyword 1", ""),
            row.get("Solution keyword 2", ""),
            row.get("Solution description", ""),
        ])

    wb.save(path)

out_dir = Path("./tmp/import-smoke")
out_dir.mkdir(parents=True, exist_ok=True)

create_rows = [
    {
        "Company": "Smoke Org AG",
        "Website": "https://smoke-org.example",
        "LinkedIn": "https://www.linkedin.com/company/smoke-org/",
        "Category NEW": "Additive Manufacturing",
        "Cantone": "Bern",
        "Email organization": "hello@smoke-org.example",
        "Name personal contact": "Alice Smoke",
        "Email personal contact (1)": "alice@smoke-org.example",
        "Email personal contact (2)": "ops@smoke-org.example",
    }
]

merge_rows = [
    {
        "Company": "Smoke Org AG",
        "Website": "https://smoke-org.example",
        "LinkedIn": "",
        "Category NEW": "",
        "Cantone": "Bern",
        "Email organization": "",
        "Name personal contact": "",
        "Email personal contact (1)": "alice@smoke-org.example",
        "Email personal contact (2)": "new@smoke-org.example",
    }
]

build_file(out_dir / "import-create.xlsx", create_rows)
build_file(out_dir / "import-merge.xlsx", merge_rows)

print(f"Created smoke files in: {out_dir.resolve()}")


#
# python3 -m venv .venv
# source .venv/bin/activate
#
# python -m pip install openpyxl
#
# python scripts/generate-import-fixtures.py
# ./scripts/smoke-import.sh