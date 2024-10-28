from flask import Flask, render_template, request, jsonify, session
from chatbot import Chatbot
import uuid

app = Flask(__name__)
app.secret_key = 'your_secret_key_here'  # 세션을 위한 비밀 키 설정
chatbot = Chatbot()

@app.route('/')
def index():
    if 'user_id' not in session:
        session['user_id'] = str(uuid.uuid4())
    return render_template('index.html')

@app.route('/chat', methods=['POST'])
def chat():
    user_input = request.form['userInput']
    user_id = session.get('user_id')
    
    response = chatbot.generate_response(user_id, user_input)
    
    print(f"Sending response: {response}")  # 디버깅을 위한 로그 추가
    return jsonify({'response': response}), 200, {'Content-Type': 'application/json; charset=utf-8'}

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)