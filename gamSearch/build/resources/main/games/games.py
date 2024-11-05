import json
from datetime import datetime

# 'games' 폴더 안의 'games.json' 파일 경로
input_file_path = 'src/main/resources/games/games.json'

# JSON 파일 읽기 (UTF-8 인코딩 지정)
with open(input_file_path, 'r', encoding='utf-8') as input_file:
    data = json.load(input_file)

# 특정 키와 값 추출하기
extracted_data = []

# 날짜 변환 함수
def convert_date(date_str):
    try:
        # "Feb 3, 2020" 형식 처리
        return datetime.strptime(date_str, '%b %d, %Y').strftime('%Y-%m-%d')
    except ValueError:
        try:
            # "Feb, 2020" 형식 처리
            return datetime.strptime(date_str, '%b %Y').strftime('%Y-%m-01')
        except ValueError:
            return None  # 잘못된 날짜 형식이 있을 경우 None 반환
        

# 긍정적 비율 평가 변환 함수
def determine_evaluation(positive_rate):
    if positive_rate >= 95:
        return "압도적으로 긍정적"
    elif 80 <= positive_rate < 95:
        return "매우 긍정적"
    elif 70 <= positive_rate < 80:
        return "대체로 긍정적"
    elif 40 <= positive_rate < 70:
        return "복합적"
    elif 20 <= positive_rate < 40:
        return "대체로 부정적"
    elif 10 <= positive_rate < 20:
        return "부정적"
    else:
        return "압도적으로 부정적"

# 데이터 구조에 따라 반복하여 필요한 정보 추출
for game_id, game_info in data.items():


    release_date = game_info.get('release_date')
    recommendations = game_info.get('recommendations', 0)
    supported_languages = game_info.get('supported_languages', [])
    positive = game_info.get('positive', 0)
    negative = game_info.get('negative', 0)
    tags = game_info.get('tags', {})
    if positive + negative > 0:  # 분모가 0이 아닌 경우에만 계산
        positive_rate = int((positive / (positive + negative)) * 100)
    else:
        positive_rate = 0  # 긍정적인 피드백이 없는 경우 0으로 설정

    evaluation = determine_evaluation(positive_rate)

    # 1. supported_languages에 'Korean'이 포함된 경우만
    if 'Korean' in supported_languages:
        # 2. positive 또는 negative가 0보다 크고, recommendations 가 0보다 큰 경우
        if (positive or negative) > 0 and recommendations > 0:
            # 3. tag에서 키값(태그명)만 가져오기

            
            tags_keys = list(tags.keys())  # 딕셔너리의 키 값만 리스트로 저장

            # 날짜 저장시 0000-00-00 형태로 저장.
            formatted_release_date = convert_date(release_date)

            # game_id 저장시 문자열이아니라 숫자로 저장.
            numeric_game_id = int(game_id)

            # 필요한 정보 추출
            extracted_info = {
                'app_id': numeric_game_id,
                'name': game_info.get('name'),
                'release_date': formatted_release_date,
                'price': game_info.get('price'),
                'recommendations': recommendations,
                'supported_languages': supported_languages,
                'categories': game_info.get('categories'),
                'genres': game_info.get('genres'),
                'positive': positive,
                'negative': negative,
                'positive_rate' : positive_rate,
                'evaluation' : evaluation,
                'tags': tags_keys  # 딕셔너리에서 키만 저장
            }

            # extracted_info를 리스트에 추가
            extracted_data.append(extracted_info)

# 결과 출력
print("추출한 데이터:")
for item in extracted_data:
    print(item)

# 선택적으로 추출한 데이터를 파일에 저장할 수 있음
output_file_path = 'src/main/resources/games/extracted_games_data.json'
with open(output_file_path, 'w', encoding='utf-8') as output_file:
    json.dump(extracted_data, output_file, indent=4)

print(f"\n추출한 데이터가 '{output_file_path}'에 저장되었습니다.")
