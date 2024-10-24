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
    return jsonify({'response': bot_response}), 200, {'Content-Type': 'application/json; charset=utf-8'}
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)


