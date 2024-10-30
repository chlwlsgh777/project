import json
import logging
import mysql.connector

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class Chatbot:
    def __init__(self):
        try:
            self.db = mysql.connector.connect(
                host="localhost",
                user="root",
                password="1234",
                database="steamgame_db"
            )
            self.cursor = self.db.cursor(dictionary=True)
            logger.info("Database connected successfully")
        except mysql.connector.Error as err:
            logger.error(f"Database connection failed: {err}")
            self.db = None
            self.cursor = None
        
        self.conversation_state = {}

    def start_conversation(self):
        return {
            "games": [],
            "message": (
                "어떤 스팀 게임을 추천해 드릴까요?\n"
                "선호하는 장르나 태그가 있으시면 입력해주세요\n"
                "(예: 액션, RPG, 공포, 시뮬레이션 등)\n\n"
                "여러 키워드로 검색하고 싶으시다면 콤마(,)로 구분하여 입력해주세요\n"
                "(예: RPG, 오픈 월드)\n\n"
                "만약 없으시다면 '랜덤'을 입력해주세요. 랜덤으로 추천해 드릴게요."
            )
        }

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
                all_match = result['all_match']
                logging.info(f"All match: {all_match}, Keywords count: {len(keywords)}")
                return all_match == 1  # 모든 키워드가 매치되면 True 반환
            else:
                logging.warning("No result returned from the query")
                return False
        except Exception as e:
            logging.error(f"Error executing query: {e}")
            return False
    
    def generate_response(self, user_id, user_input):
        logger.info(f"Generating response for user {user_id} with input: {user_input}")

        # '시작' 또는 'start' 입력 처리
        if user_input.lower() in ['start', '시작']:
            return self.start_new_conversation(user_id)

        if user_id not in self.conversation_state:
            return self.start_new_conversation(user_id)

        state = self.conversation_state[user_id]
        logger.info(f"Current state for user {user_id}: {state}")

        if state['stage'] == 'initial':
            if user_input.lower() == '랜덤':
                logger.info(f"User {user_id} requested random games")
                return self.recommend_random_games(user_id)
            elif self.is_valid_genre_or_tag(user_input):
                state['preferences']['genre'] = user_input
                state['stage'] = 'category'
                logger.info(f"User {user_id} provided valid genre/tag: {user_input}")
                return {"message": "싱글 / 멀티 / 협동 중 선택해 주세요.", "games": []}
            else:
                logger.info(f"User {user_id} provided invalid genre/tag: {user_input}")
                return {
                    "message": (
                        "죄송합니다. 입력하신 장르나 태그를 찾을 수 없습니다.\n" 
                        "다른 장르나 태그를 입력해 주세요.\n "
                        "(예: 액션, RPG, 공포 등)\n"
                        "여러 키워드로 검색하고 싶으시다면 콤마(,)로 구분하여 입력해주세요\n"
                        "(예: RPG, 오픈 월드)\n"
                        "또는 '랜덤'을 입력하시면 랜덤으로 게임을 추천해 드립니다."
                    ),
                    "games": []
                }

        elif state['stage'] == 'category':
            if user_input.lower() in ['싱글', '멀티', '협동']:
                state['preferences']['category'] = user_input
                state['stage'] = 'sort'
                logger.info(f"User {user_id} provided category: {user_input}")
                return {"message": "추천순 / 출시일순 / 랜덤 중 선택해 주세요.", "games": []}
            else:
                return {"message": "죄송합니다. '싱글', '멀티', '협동' 중 하나를 선택해 주세요.", "games": []}

        elif state['stage'] == 'sort':
            if user_input.lower() in ['추천순', '출시일순', '랜덤']:
                state['preferences']['sort'] = user_input
                logger.info(f"User {user_id} provided sort preference: {user_input}")
                return self.recommend_games(user_id)
            else:
                return {"message": "죄송합니다. '추천순', '출시일순', '랜덤' 중 하나를 선택해 주세요.", "games": []}

        elif state['stage'] in ['more', 'more_random']:
            if user_input.lower() == '예':
                logger.info(f"User {user_id} requested more games")
                return self.show_more_games(user_id) if state['stage'] == 'more' else self.show_more_random_games(user_id)
            elif user_input.lower() == '아니요':
                logger.info(f"User {user_id} finished recommendation, restarting conversation")
                return self.start_new_conversation(user_id)
            else:
                message = "죄송합니다. '예' 또는 '아니요'로 대답해 주세요. "
                message += "다른 목록을 보여드릴까요? (예 / 아니요)" if state['stage'] == 'more' else "다른 랜덤 게임을 더 보여드릴까요? (예 / 아니요)"
                return {"message": message, "games": []}

        else:
            logger.warning(f"Unexpected stage for user {user_id}: {state['stage']}")
            return self.start_new_conversation(user_id)

    def start_new_conversation(self, user_id):
        self.conversation_state[user_id] = {'stage': 'initial', 'preferences': {}, 'results': [], 'current_index': 0}
        logger.info(f"Starting new conversation for user {user_id}")
        return self.start_conversation()


    def recommend_games(self, user_id):
        state = self.conversation_state[user_id]
        preferences = state['preferences']

        keywords = [keyword.strip().lower() for keyword in preferences['genre'].split(',')]

        query = """
        SELECT g.name, g.app_id, g.release_date, g.recommendations,
               GROUP_CONCAT(DISTINCT gg.genre SEPARATOR ', ') AS genres,
               GROUP_CONCAT(DISTINCT gt.tag SEPARATOR ', ') AS tags,
               GROUP_CONCAT(DISTINCT gc.categories SEPARATOR ', ') AS categories
        FROM game g
        LEFT JOIN game_genre gg ON g.id = gg.game_id
        LEFT JOIN game_tag gt ON g.id = gt.game_id
        LEFT JOIN game_categories gc ON g.id = gc.game_id
        WHERE gc.categories LIKE %s
        GROUP BY g.id
        HAVING 
        """

        having_conditions = []
        params = [f"%{preferences['category']}%"]

        for keyword in keywords:
            having_conditions.append("""
                (LOWER(GROUP_CONCAT(DISTINCT gg.genre SEPARATOR ', ')) LIKE %s 
                 OR LOWER(GROUP_CONCAT(DISTINCT gt.tag SEPARATOR ', ')) LIKE %s
                 OR LOWER(GROUP_CONCAT(DISTINCT gc.categories SEPARATOR ', ')) LIKE %s)
            """)
            params.extend([f"%{keyword}%", f"%{keyword}%", f"%{keyword}%"])

        query += " AND ".join(having_conditions)

        if preferences['sort'] == '추천순':
            query += " ORDER BY g.recommendations DESC"
        elif preferences['sort'] == '출시일순':
            query += " ORDER BY g.release_date DESC"
        else:
            query += " ORDER BY RAND()"

        query += " LIMIT 100"

        logger.info(f"Executing query: {query}")
        logger.info(f"With parameters: {params}")

        try:
            self.cursor.execute(query, params)
            state['results'] = self.cursor.fetchall()
            state['current_index'] = 0

            if not state['results']:
                logger.info("No results found for the given criteria.")
                return "죄송합니다. 조건에 맞는 게임을 찾을 수 없습니다."

            return self.show_more_games(user_id)
        
        except mysql.connector.Error as err:
            logger.error(f"Database error in recommend_games: {err}")
            return "죄송합니다. 게임 추천 중 오류가 발생했습니다."

        except Exception as e:
            logger.error(f"Unexpected error in recommend_games: {e}")
            return "죄송합니다. 예기치 못한 오류가 발생했습니다."
        

    def recommend_random_games(self, user_id):
        query = """
        SELECT g.name, g.app_id, g.release_date, g.recommendations,
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
        LIMIT 10
        """

        try:
            self.cursor.execute(query)
            results = self.cursor.fetchall()

            if not results:
                return "죄송합니다. 랜덤 추천할 게임을 찾을 수 없습니다."

            self.conversation_state[user_id]['results'] = results
            self.conversation_state[user_id]['current_index'] = 0
            self.conversation_state[user_id]['stage'] = 'more_random'

            return self.show_more_random_games(user_id)

        except mysql.connector.Error as err:
            logger.error(f"Database error in recommend_random_games: {err}")
            return "죄송합니다. 랜덤 게임 추천 중 오류가 발생했습니다."

        except Exception as e:
            logger.error(f"Unexpected error in recommend_random_games: {e}")
            return "죄송합니다. 예기치 못한 오류가 발생했습니다."
    
    def show_more_games(self, user_id):
        state = self.conversation_state[user_id]
        start = state['current_index']
        end = start + 5
        current_results = state['results'][start:end]

        # 현재 결과가 있는지 확인
        if current_results:
            state['current_index'] = end
            state['stage'] = 'more'
            formatted_results = self.format_game_results(current_results)

            # 다음 결과가 있는지 확인
            if end >= len(state['results']):
                formatted_results['message'] = "이것이 마지막 목록입니다. 새로운 추천을 원하시면 '시작'을 입력해주세요."
        else:
            formatted_results = {"message": "더 이상 추천할 게임이 없습니다. 새로운 추천을 원하시면 '시작'을 입력해주세요", "games": []}

        return formatted_results
    
    def show_more_random_games(self, user_id):
        state = self.conversation_state[user_id]
        results = state['results']
        current_index = state['current_index']
    
        if current_index >= len(results):
            return {"message": "더 이상 추천할 랜덤 게임이 없습니다. 새로운 추천을 원하시면 '시작'을 입력해주세요.", "games": []}
    
        end_index = min(current_index + 5, len(results))
        games_to_show = results[current_index:end_index]
    
        state['current_index'] = end_index
        state['stage'] = 'more_random'
    
        response = self.format_game_results(games_to_show)
        if end_index < len(results):
            response["message"] = "다른 랜덤 게임을 더 보여드릴까요? (예 / 아니요)"
        else:
            response["message"] = "더 이상 추천할 랜덤 게임이 없습니다. 새로운 추천을 원하시면 '시작'을 입력해주세요."
    
        return response

   

    def format_game_results(self, results):
        games_list = [
            {
                "name": game['name'],
                "app_id": game['app_id'],
                "release_date": game['release_date'].strftime('%Y-%m-%d'),
                "genres": game['genres'],
                "tags": game['tags']
            }
            for game in results
        ]
        
        return {
            "games": games_list,
            "message": "다른 목록을 보여드릴까요? (예 / 아니요)"
        }