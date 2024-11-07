from flask import Blueprint, jsonify, render_template, request
import requests
import logging
from bs4 import BeautifulSoup
from cachetools import TTLCache

discount_bp = Blueprint('discount', __name__)

# 상수 정의
INITIAL_PAGE_SIZE = 20
DEFAULT_PAGE_SIZE = 10
CACHE_MAXSIZE = 100
CACHE_TTL = 3600  # 1시간

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Steam 할인 페이지 URL
STEAM_DISCOUNT_URL = 'https://store.steampowered.com/search/?specials=1&supportedlang=koreana&ndl=1'

# 캐시 설정
cache = TTLCache(maxsize=CACHE_MAXSIZE, ttl=CACHE_TTL)

def fetch_discounted_games():
    """Steam의 할인 페이지를 스크래핑하여 할인 게임 목록을 캐시에 저장"""
    try:
        if 'discounted_games' not in cache:
            response = requests.get(STEAM_DISCOUNT_URL)
            response.raise_for_status()
            soup = BeautifulSoup(response.text, 'html.parser')
            
            discounted_games = []
            game_containers = soup.select('.search_result_row')  # 할인 게임 컨테이너
            
            for game in game_containers:
                title = game.select_one('.title').get_text(strip=True)
                discount_percent = game.select_one('.search_discount span')
                original_price = game.select_one('.search_price strike')
                discounted_price = game.select_one('.search_price').get_text(strip=True).split()[-1]
                image_url = game.select_one('img').get('src')
                
                if discount_percent:
                    discounted_games.append({
                        'name': title,
                        'discountPercent': discount_percent.get_text(strip=True),
                        'originalPrice': original_price.get_text(strip=True) if original_price else None,
                        'finalPrice': discounted_price,
                        'imageUrl': image_url
                    })
            cache['discounted_games'] = discounted_games
        return cache['discounted_games']
    except requests.RequestException as e:
        logger.error(f"Steam 할인 페이지에서 데이터를 가져오는 중 오류 발생: {e}")
        return []

def get_discounted_games(page, size):
    games = fetch_discounted_games()
    start = page * size
    end = start + size
    return games[start:end], len(games) > end

@discount_bp.route('/discount')
def discount_page():
    initial_games, has_more = get_discounted_games(0, INITIAL_PAGE_SIZE)
    return render_template('discount.html', games=initial_games, has_more=has_more)

@discount_bp.route('/api/games', methods=['GET'])
def get_more_games():
    try:
        page = int(request.args.get('page', 0))
        size = int(request.args.get('size', DEFAULT_PAGE_SIZE))
        games, has_more = get_discounted_games(page, size)
        return jsonify({
            'games': games,
            'hasMore': has_more
        })
    except Exception as e:
        logger.error(f"get_more_games에서 오류 발생: {e}")
        return jsonify({'error': 'Internal server error'}), 500
