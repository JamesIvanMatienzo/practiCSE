import pandas as pd
import json

def process_general_info():
    df = pd.read_csv('PractiCSE_Datasource - general_information.csv')
    questions = []
    for _, row in df.iterrows():
        questions.append({
            "category": "General Information",
            "subCategory": "General",
            "questionText": str(row['Question']).strip(),
            "referenceText": None,
            "correctAnswer": str(row['Correct Answer']).strip(),
            "wrongChoices": [ans.strip() for ans in str(row['Wrong Choices']).split(',')]
        })
    return questions

def process_numerical():
    df = pd.read_csv('PractiCSE_Datasource - numerical_ability.csv')
    questions = []
    for _, row in df.iterrows():
        questions.append({
            "category": "Numerical Ability",
            "subCategory": "Pattern & Arithmetic",
            "questionText": str(row['Numerical Ability']).strip(),
            "referenceText": None,
            "correctAnswer": str(row['Answers']).strip(),
            "wrongChoices": [ans.strip() for ans in str(row['Wrong Answers (from choices)']).split(',')]
        })
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