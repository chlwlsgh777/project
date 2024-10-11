import json

# JSON 파일 경로 (파일이 저장된 경로)
input_file_path = 'src/main/resources/games/extracted_games_data.json'

# JSON 파일 읽기 (UTF-8 인코딩 지정)
with open(input_file_path, 'r', encoding='utf-8') as input_file:
    data = json.load(input_file)

# 딕셔너리 개수 확인 (리스트 안의 항목 개수)
dict_count = len(data)

print(f"JSON 파일 안에 있는 게임 개수: {dict_count}")

# 3498