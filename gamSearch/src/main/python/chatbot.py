import logging
import random
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
        return (
            "어떤 스팀 게임을 추천해 드릴까요?\n"
            "원하시는 장르나 태그가 있으시면 입력해주세요(예: 액션, RPG, 공포 등)\n"
            "만약 없으시다면 '랜덤'을 입력해주세요. 랜덤으로 추천해 드릴게요."
        )

    def generate_response(self, user_id, user_input):
        logger.info(f"Generating response for user {user_id} with input: {user_input}")

        # 대화 상태 초기화
        if user_id not in self.conversation_state or user_input.lower() == 'start':
            self.conversation_state[user_id] = {'stage': 'initial', 'preferences': {}, 'results': [], 'current_index': 0}
            logger.info(f"Starting new conversation for user {user_id}")
            return self.start_conversation()
        
        state = self.conversation_state[user_id]
        logger.info(f"Current state for user {user_id}: {state}")

        if state['stage'] == 'initial':
            if user_input.lower() == '랜덤':
                logger.info(f"User {user_id} requested random games")
                return self.recommend_random_games(user_id)
            else:
                state['preferences']['genre'] = user_input
                state['stage'] = 'category'
                logger.info(f"User {user_id} provided genre: {user_input}")
                return "싱글/ 멀티 / 협동 중에 골라주세요"

        elif state['stage'] == 'category':
            state['preferences']['category'] = user_input
            state['stage'] = 'sort'
            logger.info(f"User {user_id} provided category: {user_input}")
            return "추천순/ 출시일순/ 랜덤 중 골라주세요"

        elif state['stage'] == 'sort':
            state['preferences']['sort'] = user_input
            logger.info(f"User {user_id} provided sort preference: {user_input}")
            return self.recommend_games(user_id)

        elif state['stage'] == 'more':
            if user_input.lower() == '예':
                logger.info(f"User {user_id} requested more games")
                return self.show_more_games(user_id)
            else:
                logger.info(f"User {user_id} finished recommendation, restarting conversation")
                self.conversation_state[user_id] = {'stage': 'initial', 'preferences': {}, 'results': [], 'current_index': 0}
                return self.start_conversation()

    def recommend_random_games(self, user_id):
        query = """
        SELECT g.name, g.app_id, g.release_date, 
               GROUP_CONCAT(DISTINCT gg.genre SEPARATOR ', ') AS genres,
               GROUP_CONCAT(DISTINCT gt.tag SEPARATOR ', ') AS tags
        FROM game g
        JOIN game_genre gg ON g.id = gg.game_id
        JOIN game_tag gt ON g.id = gt.game_id
        GROUP BY g.id
        ORDER BY RAND()
        LIMIT 5
        """
        
        self.cursor.execute(query)
        results = self.cursor.fetchall()

        response = "랜덤으로 추천된 게임 목록입니다:\n\n"
        response += self.format_game_results(results)
        response += "\n다시 추천해 드릴까요? (예 / 아니요)"

        self.conversation_state[user_id]['stage'] = 'more'
        return response

    def recommend_games(self, user_id):
        state = self.conversation_state[user_id]
        preferences = state['preferences']

        query = """
        SELECT g.name, g.app_id, g.release_date, 
               GROUP_CONCAT(DISTINCT gg.genre SEPARATOR ', ') AS genres,
               GROUP_CONCAT(DISTINCT gt.tag SEPARATOR ', ') AS tags,
               GROUP_CONCAT(DISTINCT gc.categories SEPARATOR ', ') AS categories
        FROM game g
        LEFT JOIN game_genre gg ON g.id = gg.game_id
        LEFT JOIN game_tag gt ON g.id = gt.game_id
        LEFT JOIN game_categories gc ON g.id = gc.game_id
        WHERE (gg.genre LIKE %s OR gt.tag LIKE %s)
        AND gc.categories LIKE %s
        """

        params = [f"%{preferences['genre']}%", f"%{preferences['genre']}%", f"%{preferences['category']}%"]

        if preferences['sort'] == '추천순':
            query += " GROUP BY g.id ORDER BY g.recommendations DESC"
        elif preferences['sort'] == '출시일순':
            query += " GROUP BY g.id ORDER BY g.release_date DESC"
        else:
            query += " GROUP BY g.id ORDER BY RAND()"

        query += " LIMIT 100"

        try:
            self.cursor.execute(query, params)
            state['results'] = self.cursor.fetchall()
            state['current_index'] = 0

            return self.show_more_games(user_id)
        except mysql.connector.Error as err:
            logger.error(f"Database error in recommend_games: {err}")
            return "죄송합니다. 게임 추천 중 오류가 발생했습니다."
        except Exception as e:
            logger.error(f"Unexpected error in recommend_games: {e}")
            return "죄송합니다. 예기치 못한 오류가 발생했습니다."

    def show_more_games(self, user_id):
        state = self.conversation_state[user_id]
        start = state['current_index']
        end = start + 5
        current_results = state['results'][start:end]

        if not current_results:
            return "더 이상 추천할 게임이 없습니다. 새로운 추천을 원하시면 아무거나 입력해주세요"

        response = self.format_game_results(current_results)
        response += "\n다른 목록을 보여드릴까요? (예 / 아니요)"

        state['current_index'] = end
        state['stage'] = 'more'
        return response

    def format_game_results(self, results):
        response = ""
        for game in results:
            response += f"게임 이름: {game['name']}\n"
            response += f"앱 ID: {game['app_id']}\n"
            response += f"출시일: {game['release_date']}\n"
            response += f"장르: {game['genres']}\n"
            response += f"태그: {game['tags']}\n\n"
        return response
