from flask import Flask, render_template, request, jsonify, session
from chatbot import Chatbot
import uuid
import json

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

    # Chatbot 인스턴스에서 응답 생성
    response_data = chatbot.generate_response(user_id, user_input)

    # 응답이 이미 JSON 형식인지 확인
    if isinstance(response_data, dict):
        return jsonify(response_data)
    else:
        # 문자열 응답을 JSON 형식으로 변환
        return jsonify({"message": response_data, "games": []})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)