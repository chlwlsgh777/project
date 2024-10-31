from flask import Flask, render_template, request, jsonify, session
from chatbot import Chatbot
import uuid

app = Flask(__name__)
app.secret_key = 'your_secret_key_here'

chatbot = Chatbot()

@app.route('/')
def index():
    if 'user_id' not in session:
        session['user_id'] = str(uuid.uuid4())
    return render_template('chatbot.html')

@app.route('/chat', methods=['POST'])
def chat():
    user_input = request.form.get('userInput', '')
    user_id = session.get('user_id')

    response_data = chatbot.generate_response(user_id, user_input)

    if isinstance(response_data, dict):
        if response_data.get('message') == "핵심 카테고리를 선택해주세요":
            response_data['buttons'] = ['싱글', '멀티', '협동', 'PvP', 'MMO']
        
        elif response_data.get('message') == "정렬 방식을 선택해 주세요":
            response_data['buttons'] = ['추천순', '출시일순', '랜덤']
       
        elif response_data.get('message') == "다른 목록을 보여드릴까요?":
            response_data['buttons'] = ['예', '아니요']
     
        elif response_data.get('message') == "다른 랜덤 게임을 더 보여드릴까요?":
            response_data['buttons'] = ['예', '아니요']
        
        elif response_data.get('message') == "죄송합니다. 조건에 맞는 게임을 찾을 수 없습니다.":
            response_data['buttons'] = ['다시 질문하기']
        
        elif response_data.get('message') == "더 이상 추천할 게임이 없습니다.\n 새로운 추천을 원하시면 '다시 질문하기'를 눌러주세요.":
            response_data['buttons'] = ['다시 질문하기']

    
        elif response_data.get('message') == "마지막 목록입니다.\n 새로운 추천을 원하시면 '다시 질문하기'를 눌러주세요.":
            response_data['buttons'] = ['다시 질문하기']




            

    
        return jsonify(response_data)
    else:
        return jsonify({"message": response_data, "games": []})

if __name__ == '__main__':    app.run(host='0.0.0.0', port=5000, debug=True)