import pandas as pd
import json
import csv
import io

# ---------------------------------------------------------------------------
# Helper: RFC-4180 compliant choice parser
# ---------------------------------------------------------------------------
# The CSV wrong-choices column contains comma-separated values that may
# themselves contain commas inside quoted strings (e.g. "25,000").  A naive
# str.split(',') shatters those numbers.  Using csv.reader honours quotes.
# ---------------------------------------------------------------------------

def parse_csv_choices(raw) -> list[str]:
    """Parse a comma-separated string of answer choices.
    
    Handles the tricky case where numbers contain commas as thousands
    separators (e.g. "30,000" or "$1,656").  Those commas must NOT be
    treated as delimiters between choices.

    Strategy: split on commas that are *delimiters* — i.e. commas that are
    NOT part of a numeric thousands pattern.  A thousands-separator comma
    is one preceded by a digit and followed by exactly 3 digits (then a
    non-digit or end-of-string).
    
    Examples:
        '23, 25, 32, None of The Above'           -> ['23', '25', '32', 'None of The Above']
        '30,000, 32,000, 33,000, None of Above'   -> ['30,000', '32,000', '33,000', 'None of Above']
        '$1,656, $1,294, $984'                     -> ['$1,656', '$1,294', '$984']
    """
    import re
    if pd.isna(raw) or str(raw).strip().lower() == 'nan':
        return []
    
    text = str(raw).strip()
    
    # Temporarily replace thousands-separator commas with a placeholder.
    # Pattern: digit + comma + exactly 3 digits followed by non-digit or end.
    placeholder = '\x00'
    text = re.sub(r'(\d),(\d{3})(?=\D|$)', rf'\1{placeholder}\2', text)
    # Apply twice to handle numbers like 1,000,000
    text = re.sub(r'(\d),(\d{3})(?=\D|$)', rf'\1{placeholder}\2', text)
    
    # Now split on remaining commas (the true delimiters)
    parts = text.split(',')
    
    choices = []
    for part in parts:
        # Restore thousands-separator commas
        restored = part.replace(placeholder, ',').strip()
        if restored and restored.lower() != 'nan':
            choices.append(restored)
    return choices


def is_valid_question(question: dict, min_wrong: int = 3) -> bool:
    """Validate that a question has meaningful text and enough wrong choices."""
    qt = question.get("questionText", "").strip()
    ca = question.get("correctAnswer", "").strip()
    wc = question.get("wrongChoices", [])

    if not qt or qt.lower() == 'nan':
        return False
    if not ca or ca.lower() == 'nan':
        return False
    if len(wc) < min_wrong:
        return False
    # Reject rows that are duplicate header lines embedded in the CSV
    if qt.lower().startswith("numerical ability") and "answers" in ca.lower():
        return False
    return True


def process_general_info():
    df = pd.read_csv('PractiCSE_Datasource - general_information.csv')
    questions = []
    for _, row in df.iterrows():
        q = {
            "category": "General Information",
            "subCategory": "General",
            "questionText": str(row['Question']).strip(),
            "referenceText": None,
            "correctAnswer": str(row['Correct Answer']).strip(),
            "wrongChoices": parse_csv_choices(row['Wrong Choices'])
        }
        if is_valid_question(q):
            questions.append(q)
    return questions


def process_numerical():
    """Read numerical_ability CSV using Python's csv module for robust parsing.
    
    The CSV has inconsistent quoting — some rows have spaces before quoted
    fields which causes pandas' C parser to miscount columns. We use the
    Python csv module which handles this gracefully.
    """
    questions = []
    with open('PractiCSE_Datasource - numerical_ability.csv', 'r', encoding='utf-8') as f:
        reader = csv.reader(f, skipinitialspace=True)
        header = next(reader)  # Skip header row
        
        for row in reader:
            if len(row) < 4:
                continue  # Malformed row
            
            # Columns: Index, Question, Answer, Wrong Choices
            # The wrong-choices column may span multiple fields if it contained
            # commas outside quotes, so we rejoin everything from col 3 onward
            index_val = row[0].strip()
            question_text = row[1].strip()
            answer = row[2].strip()
            # Rejoin remaining columns as the wrong-choices string, then parse
            wrong_raw = ','.join(row[3:])
            
            q = {
                "category": "Numerical Ability",
                "subCategory": "Pattern & Arithmetic",
                "questionText": question_text,
                "referenceText": None,
                "correctAnswer": answer,
                "wrongChoices": parse_csv_choices(wrong_raw)
            }
            if is_valid_question(q):
                questions.append(q)
    return questions


def process_verbal():
    df = pd.read_csv('PractiCSE_Datasource - verbal_ability.csv')
    questions = []
    for _, row in df.iterrows():
        # Example: Extracting Reading Comprehension
        if pd.notna(row['Question: Reading Comprehension']):
            questions.append({
                "category": "Verbal Ability",
                "subCategory": "Reading Comprehension",
                "questionText": str(row['Question: Reading Comprehension']).strip(),
                "referenceText": str(row['Reference Text']).strip() if pd.notna(row['Reference Text']) else None,
                "correctAnswer": str(row['Correct Answer.1']).strip(),
                "wrongChoices": [ans.strip() for ans in str(row['Wrong Answers.1']).split('\n') if ans.strip()]
            })
        # Note: You can replicate the block above for Synonyms, Grammar, and Spelling columns in your CSV.
    return questions


# Combine all and export
all_questions = process_general_info() + process_numerical() + process_verbal()

with open('questions.json', 'w', encoding='utf-8') as f:
    json.dump(all_questions, f, indent=4, ensure_ascii=False)

print(f"Successfully generated questions.json with {len(all_questions)} questions!")

# --- Summary report ---
categories = {}
for q in all_questions:
    cat = q['category']
    categories[cat] = categories.get(cat, 0) + 1
for cat, count in sorted(categories.items()):
    print(f"  {cat}: {count}")

# Warn about any remaining questions with < 3 wrong choices (should be zero)
bad = [q for q in all_questions if len(q['wrongChoices']) < 3]
if bad:
    print(f"\n[!] WARNING: {len(bad)} questions still have < 3 wrong choices!")
    for q in bad[:5]:
        print(f"    Q: {q['questionText'][:60]}  choices={q['wrongChoices']}")
else:
    print("\n[OK] All questions have >= 3 wrong choices.")