from flask import Flask, render_template, request, jsonify, session
from chatbot import Chatbot
from models.discount import discount_bp
import uuid

app = Flask(__name__)
app.secret_key = 'your_secret_key_here'

# 할인 관련 Blueprint 등록
app.register_blueprint(discount_bp)

chatbot = Chatbot()

@app.route('/chatbot')
def chatbot_page():
    if 'user_id' not in session:
        session['user_id'] = str(uuid.uuid4())
    return render_template('chatbot.html')

@app.route('/chat', methods=['POST'])
def chat():
    user_input = request.form.get('userInput', '')
    user_id = session.get('user_id')

    response_data = chatbot.generate_response(user_id, user_input)

    return jsonify(response_data)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)