from flask import Flask, render_template, request, jsonify, session
from chatbot import Chatbot
import uuid

app = Flask(__name__)
app.secret_key = 'your_secret_key_here'

chatbot = Chatbot()

@app.route('/chatbot')
def index():
    if 'user_id' not in session:
        session['user_id'] = str(uuid.uuid4())
    return render_template('chatbot.html')

@app.route('/chat', methods=['POST'])
def chat():
    user_input = request.form.get('userInput', '')
    user_id = session.get('user_id')

    response_data = chatbot.generate_response(user_id, user_input)

    # 응답 데이터를 JSON으로 반환합니다.
    return jsonify(response_data)
    

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
