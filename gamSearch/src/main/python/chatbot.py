import logging
import random
from transformers import AutoTokenizer, AutoModelForCausalLM
from transformers import BertTokenizer, BertForTokenClassification
import torch
import mysql.connector

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class Chatbot:
    def __init__(self, model_name='skt/kogpt2-base-v2', keyword_model_name='bert-base-multilingual-cased'):
        # 모델 초기화
        self.tokenizer = AutoTokenizer.from_pretrained(model_name)
        self.model = AutoModelForCausalLM.from_pretrained(model_name)
        self.keyword_tokenizer = BertTokenizer.from_pretrained(keyword_model_name)
        self.keyword_model = BertForTokenClassification.from_pretrained(keyword_model_name)

        # 데이터베이스 연결 초기화
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

    def extract_keywords(self, text):
        inputs = self.keyword_tokenizer(text, return_tensors="pt")
        outputs = self.keyword_model(**inputs).logits
        predictions = torch.argmax(outputs, dim=2)
        
        keywords = [word for idx, word in enumerate(text.split()) if predictions[0][idx] == 1]
        logger.info(f"Extracted keywords: {keywords}")
        return keywords if keywords else [text]  # 키워드가 없으면 입력 텍스트 전체를 사용

    def search_db(self, keywords):
        if not self.cursor:
            logger.error("Database cursor is not available")
            return None

        # 키워드를 사용하여 장르나 태그를 검색
        query = """
        SELECT g.name, g.app_id, g.release_date,
        GROUP_CONCAT(DISTINCT gg.genre SEPARATOR ', ') AS genres,
        SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT gt.tag ORDER BY gt.tag SEPARATOR ', '), ', ', 10) AS tags
        FROM game g
        JOIN game_genre gg ON g.id = gg.game_id
        JOIN game_tag gt ON g.id = gt.game_id
        WHERE gg.genre LIKE %s OR gt.tag LIKE %s
        GROUP BY g.id
        ORDER BY RAND()
        LIMIT 5
        """
    
        # 각 키워드에 대해 쿼리 실행
        results = []
        for keyword in keywords:
            params = (f'%{keyword}%', f'%{keyword}%')
            logger.info(f"Executing query: {query} with params: {params}")
        
            try:
                self.cursor.execute(query, params)
                results.extend(self.cursor.fetchall())
            except mysql.connector.Error as err:
                logger.error(f"Database query failed: {err}")
    
        # 결과가 5개를 초과하면 랜덤하게 5개만 선택
        if len(results) > 5:
            results = random.sample(results, 5)
    
        logger.info(f"Query returned {len(results)} results")
        return results

    def format_response(self, game_info):
        response = f"게임 이름: {game_info['name']}\n"
        response += f"앱 ID: {game_info['app_id']}\n"
        response += f"출시일: {game_info['release_date']}\n"
        response += f"태그: {game_info['tags']}"
        return response

    def generate_response(self, prompt):
        keywords = self.extract_keywords(prompt)
        game_info_list = self.search_db(keywords)
    
        if game_info_list:
            responses = [self.format_response(game_info) for game_info in game_info_list]
            return "\n\n".join(responses)
        else:
            logger.warning("No game information found, using default response")
            return "죄송합니다. 해당 키워드에 맞는 게임을 찾을 수 없습니다."