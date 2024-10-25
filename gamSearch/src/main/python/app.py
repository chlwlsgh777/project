from flask import Flask, render_template, request, jsonify
from chatbot import Chatbot

app = Flask(__name__)
chatbot = Chatbot()

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/chat', methods=['POST'])
def chat():
    user_message = request.form['userInput']
    bot_response = chatbot.generate_response(user_message)
    
    # 게임 정보가 없는 경우 적절한 응답 제공
    if bot_response.strip() == "":
        bot_response = "해당 키워드로 검색된 게임이 없습니다."
    
    return jsonify({'response': bot_response}), 200, {'Content-Type': 'application/json; charset=utf-8'}

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)


