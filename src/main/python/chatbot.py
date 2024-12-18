import logging
import mysql.connector
from genre_tag_mapping import genre_tag_mapping

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class Chatbot:
    def __init__(self):
        try:
            self.db = mysql.connector.connect(
                host="localhost",
                user="root",
                password="1234",
                database="gamesearch_db"
            )
            self.cursor = self.db.cursor(dictionary=True)
            logger.info("Database connected successfully")
        
        except mysql.connector.Error as err:
            logger.error(f"Database connection failed: {err}")
            self.db = None
            self.cursor = None
        
        self.conversation_state = {}

    
    def normalize_input(self, user_input):
        # 사용자가 입력한 장르/태그를 매핑 딕셔너리로 표준화
        keywords = [genre_tag_mapping.get(word.strip().lower(), word.strip()) for word in user_input.split(',')]
        return ', '.join(keywords)

    # 챗봇 첫 부분 실행하는 함수
    def start_new_conversation(self, user_id):
        self.conversation_state[user_id] = {'stage': 'initial', 'preferences': {}, 'results': [], 'current_index': 0}
        logger.info(f"Starting new conversation for user {user_id}")
        return {
            "games": [],
            "message": (
                "어떤 스팀 게임을 추천해 드릴까요?\n"
                "선호하는 장르나 태그가 있으시면 입력해주세요\n"
                "(예: 액션, RPG, 공포, 시뮬레이션 등)\n\n"
                "여러 키워드로 검색하고 싶으시다면 콤마(,)로 구분하여 입력해주세요\n"
                "(예: 턴제, 오픈 월드, 판타지)\n\n"
                "만약 없으시다면 '랜덤'을 입력해주세요. 랜덤으로 추천해 드릴게요."
            )
        }

    # 입력받은 장르나 태그가 DB에 존재하는지 확인하는 함수 (여러개를 입력했을 경우 여러개를 모두 포함한 정보를 미리 검색해봄)
    def is_valid_genre_or_tag(self, input_text):
        keywords = [keyword.strip() for keyword in input_text.split(',')]
        placeholders = ' AND '.join(['EXISTS (SELECT 1 FROM (SELECT genre AS term FROM game_genre UNION SELECT tag AS term FROM game_tag) AS terms WHERE term LIKE %s)'] * len(keywords))
        query = f"""
        SELECT 
            CASE 
                WHEN {placeholders} THEN 1
                ELSE 0
            END as all_match
        """
        params = tuple(f"%{keyword}%" for keyword in keywords)

        logging.info(f"Executing query: {query}")
        logging.info(f"With parameters: {params}")

        try:
            self.cursor.execute(query, params)
            result = self.cursor.fetchone()
            logging.info(f"Query result: {result}")

            if result:
                return result['all_match'] == 1
            else:
                logging.warning("No result returned from the query")
                return False
        except Exception as e:
            logger.error(f"Error executing query in is_valid_genre_or_tag: {e}")
            return False


    # 응답 생성하는 함수
    def generate_response(self, user_id, user_input):
        user_input = self.normalize_input(user_input)

        logger.info(f"Generating response for user {user_id} with input: {user_input}")

        
        if user_input.lower() in ['다시 질문하기', 'start']:
            return self.start_new_conversation(user_id)

        if user_id not in self.conversation_state:
            return self.start_new_conversation(user_id)

        state = self.conversation_state[user_id]
        logger.info(f"Current state for user {user_id}: {state}")

        # 버튼 생성시 필요한 리스트(리스트에 입력한 요소들 전부 버튼으로 생성됨)
        valid_categories = ['싱글', '멀티', '협동', 'pvp', 'mmo']
        valid_sort_options = ['추천순', '출시일순', '랜덤'] # sort 옵션을 추가할 경우  recommend_games 함수에 ORDER BY 쿼리를 추가해 줘야함.

        # 사용자 입력 받는 첫 단계
        if state['stage'] == 'initial':
            
            # 랜덤 입력을 받았을 때
            if user_input.lower() == '랜덤':
                logger.info(f"User {user_id} requested random games")
                return self.recommend_random_games(user_id)
            
            # 사용자 입력을 받았을때 DB 확인후 state를  'category'로 변경 
            elif self.is_valid_genre_or_tag(user_input):
                state['preferences']['genre_or_tag'] = user_input
                state['stage'] = 'category'
                logger.info(f"User {user_id} provided valid genre/tag: {user_input}")
                return {"message": "핵심 카테고리를 선택해주세요", "games": [], "buttons": valid_categories}
            
            # DB 확인후 일치하는 정보가 없을때
            else:
                logger.info(f"User {user_id} provided invalid genre/tag: {user_input}")
                return {
                    "message": (
                        "죄송합니다. 입력하신 장르나 태그를 찾을 수 없습니다.\n\n"
                        "다른 장르나 태그를 입력해 주세요.\n "
                        "(예: 액션, RPG, 공포 등)\n\n"
                        "여러 키워드로 검색하고 싶으시다면 콤마(,)로 구분하여 입력해주세요\n"
                        "(예: 턴제, 오픈 월드, 판타지)\n\n"
                        "또는 '랜덤'을 입력하시면 랜덤으로 게임을 추천해 드립니다."
                    ),
                    "games": []
                }

        # state 'category' 일때
        elif state['stage'] == 'category':
            
            # valid_categories 에 존재하는 카테고리를 입력받았을때 state 를 sort 로 변경
            if user_input.lower() in valid_categories:
                state['preferences']['category'] = user_input
                state['stage'] = 'sort'
                logger.info(f"User {user_id} provided category: {user_input}")
                return {"message": "정렬 방식을 선택해 주세요", "games": [], "buttons": valid_sort_options}
            else:
                return {"message": "죄송합니다. 핵심 카테고리 중 하나를 선택해 주세요.", "games": [], "buttons": valid_categories}


        # state 'sort' 일때
        elif state['stage'] == 'sort':
            
            # valid_sort_options 에 존재하는 정렬방식을 입력 받았을때 입력받은 정보를 토대로 recommend_games 함수 실행.
            if user_input.lower() in valid_sort_options:
                state['preferences']['sort'] = user_input
                logger.info(f"User {user_id} provided sort preference: {user_input}")
                return self.recommend_games(user_id)
            else:
                return {"message": "죄송합니다. 정렬 방식 중 하나를 선택해 주세요.", "games": [],"buttons": valid_sort_options}
        

        # 결과가 존재하지 않을때
        elif state['stage'] == 'no_results':
            return {"message": "새로운 추천을 받으려면 '다시 질문하기'를 눌러주세요.", "games": [] , "buttons": ['다시 질문하기']}
        
        # 목록 더보기
        elif state['stage'] in ['more', 'more_random']:
            if user_input.lower() == '예':
                logger.info(f"User {user_id} requested more games")
                return self.show_more_games(user_id, is_random=(state['stage'] == 'more_random'))
            elif user_input.lower() == '아니요':
                return self.start_new_conversation(user_id)
            else:
                message = "죄송합니다. '예' 또는 '아니요'로 대답해 주세요.\n"
                message += "다른 랜덤 게임을 더 보여드릴까요?" if state['stage'] == 'more_random' else "다른 목록을 보여드릴까요?"
                return {"message": message, "games": [], "buttons": ['예', '아니요']}

        elif state['stage'] == 'end':
            return {"message": "새로운 추천을 원하시면 '다시 질문하기'를 눌러주세요.", "games": [] ,"buttons": ['다시 질문하기']}

        else:
            logger.warning(f"Unexpected stage for user {user_id}: {state['stage']}")
            return self.start_new_conversation(user_id)


    # 입력받은 조건을 토대로 게임을 검색하고 results 에 저장하는 함수
    def recommend_games(self, user_id):
        state = self.conversation_state[user_id]
        preferences = state['preferences']
        keywords = [keyword.strip().lower() for keyword in preferences['genre_or_tag'].split(',')]

        query = """
        SELECT g.name, g.app_id, g.release_date, g.recommendations, g.price,
               GROUP_CONCAT(DISTINCT gg.genre SEPARATOR ', ') AS genres,
               GROUP_CONCAT(DISTINCT gt.tag SEPARATOR ', ') AS tags,
               GROUP_CONCAT(DISTINCT gc.categories SEPARATOR ', ') AS categories
        FROM game g
        LEFT JOIN game_genre gg ON g.id = gg.game_id
        LEFT JOIN game_tag gt ON g.id = gt.game_id
        LEFT JOIN game_categories gc ON g.id = gc.game_id
        GROUP BY g.id
        HAVING 
        """
        having_conditions = ["LOWER(GROUP_CONCAT(DISTINCT gc.categories SEPARATOR ', ')) LIKE %s"]
        params = [f"%{preferences['category'].lower()}%"]

        for keyword in keywords:
            having_conditions.append("""
                (LOWER(GROUP_CONCAT(DISTINCT gg.genre SEPARATOR ', ')) LIKE %s 
                 OR LOWER(GROUP_CONCAT(DISTINCT gt.tag SEPARATOR ', ')) LIKE %s)
            """)
            params.extend([f"%{keyword}%", f"%{keyword}%"])

        query += " AND ".join(having_conditions)

        if preferences['sort'] == '추천순':
            query += " ORDER BY g.recommendations DESC"
        elif preferences['sort'] == '출시일순':
            query += " ORDER BY g.release_date DESC"
        else:
            query += " ORDER BY RAND()"

        query += " LIMIT 100"

        try:
            self.cursor.execute(query, params)
            state['results'] = self.cursor.fetchall()
            state['current_index'] = 0

            if not state['results']:
                state['stage'] = 'no_results'
                return {"message": "죄송합니다. 조건에 맞는 게임을 찾을 수 없습니다.", "games": [] ,"buttons": ['다시 질문하기']}

            return self.show_more_games(user_id)

        except mysql.connector.Error as err:
            logger.error(f"Database error in recommend_games: {err}")
            return "죄송합니다. 게임 추천 중 오류가 발생했습니다."

    # 첫 입력에 랜덤을 입력했을때 실행되는 함수
    def recommend_random_games(self, user_id):
        query = """
        SELECT g.name, g.app_id, g.release_date, g.recommendations, g.price
               GROUP_CONCAT(DISTINCT gg.genre SEPARATOR ', ') AS genres,
               GROUP_CONCAT(DISTINCT gt.tag SEPARATOR ', ') AS tags,
               GROUP_CONCAT(DISTINCT gc.categories SEPARATOR ', ') AS categories
        FROM game g
        LEFT JOIN game_genre gg ON g.id = gg.game_id
        LEFT JOIN game_tag gt ON g.id = gt.game_id
        LEFT JOIN game_categories gc ON g.id = gc.game_id
        WHERE g.recommendations >= 5000
        GROUP BY g.id
        ORDER BY RAND()
        LIMIT 500
        """

        try:
            self.cursor.execute(query)
            results = self.cursor.fetchall()

            if not results:
                return {"message": "죄송합니다. 랜덤 추천할 게임을 찾을 수 없습니다.", "games": [], "buttons": ['다시 질문하기']}

            self.conversation_state[user_id]['results'] = results
            self.conversation_state[user_id]['current_index'] = 0
            self.conversation_state[user_id]['stage'] = 'more_random'

            return self.show_more_games(user_id, is_random=True)

        except mysql.connector.Error as err:
            logger.error(f"Database error in recommend_random_games: {err}")
            return "죄송합니다. 랜덤 게임 추천 중 오류가 발생했습니다."


    # 추천 목록 더 볼때 실행되는 함수
    def show_more_games(self, user_id, is_random=False):
        state = self.conversation_state[user_id]
        results = state['results']
        current_index = state['current_index']
    
        if current_index >= len(results):
            state['stage'] = 'end'
            return {
                "message": "더 이상 추천할 게임이 없습니다.\n새로운 추천을 원하시면 '다시 질문하기'를 눌러주세요.",
                "games": [],
                "buttons": ['다시 질문하기']
            }
    
        end_index = min(current_index + 5, len(results))
        games_to_show = results[current_index:end_index]
    
        state['current_index'] = end_index
        
        if end_index >= len(results):
            state['stage'] = 'end'
            message = "마지막 추천 목록 입니다.\n새로운 추천을 원하시면 '다시 질문하기'를 눌러주세요."
            buttons = ['다시 질문하기']
        else:
            state['stage'] = 'more_random' if is_random else 'more'
            message = "다른 게임을 더 보여드릴까요?"
            buttons = ['예', '아니요']
    
        formatted_games = self.format_game_results(games_to_show)

        return {
            "message": message,
            "games": formatted_games,
            "buttons": buttons
        }

    # 결과를 저장하는 함수 
    def format_game_results(self, results):
        return [
            {
                "name": game['name'],
                "app_id": game['app_id'],
                "release_date": game['release_date'].strftime('%Y-%m-%d'),
                "categories": game['categories'],
                "genres": game['genres'],
                "tags": game['tags'],
                "price": game['price']
            }
            for game in results
        ]
