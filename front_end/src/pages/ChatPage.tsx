import React, { useEffect, useState, useRef } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import SockJS from "sockjs-client";
import { over } from "stompjs";

const ChatPage = () => {
  const { conversationId } = useParams();
  const [messages, setMessages] = useState([]);
  const [content, setContent] = useState("");
  const [stompClient, setStompClient] = useState(null);
  const chatEndRef = useRef(null);

  const token = localStorage.getItem("token"); // JWT salvo no login
  const userEmail = localStorage.getItem("email");

  // Auto scroll sempre pro fim
  const scrollToBottom = () => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  // Carregar mensagens antigas
  const loadMessages = async () => {
    try {
      const res = await axios.get(
        `http://localhost:8080/api/chat/conversations/${conversationId}/messages`,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      setMessages(res.data.content || res.data);
    } catch (err) {
      console.error("Erro ao carregar mensagens", err);
    }
  };

  // Conectar ao WebSocket
  const connectWS = () => {
    const socket = new SockJS("http://localhost:8080/ws");
    const stomp = over(socket);

    stomp.connect(
      { Authorization: `Bearer ${token}` },
      () => {
        stomp.subscribe(
          `/topic/conversations/${conversationId}`,
          (message) => {
            const msg = JSON.parse(message.body);
            setMessages((prev) => [...prev, msg]);
          }
        );
      },
      (error) => console.error("Erro WS:", error)
    );

    setStompClient(stomp);
  };

  // Enviar mensagem
  const sendMessage = async () => {
    if (!content.trim()) return;
    try {
      await axios.post(
        `http://localhost:8080/api/chat/conversations/${conversationId}/messages`,
        { content },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setContent("");
    } catch (err) {
      console.error("Erro ao enviar mensagem", err);
    }
  };

  // Hooks
  useEffect(() => {
    loadMessages();
    connectWS();
  }, [conversationId]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  return (
    <div className="chat-container" style={styles.container}>
      <div className="chat-box" style={styles.chatBox}>
        {messages.map((msg) => (
          <div
            key={msg.id}
            style={{
              ...styles.message,
              alignSelf:
                msg.senderId === userEmail ? "flex-end" : "flex-start",
              backgroundColor:
                msg.senderId === userEmail ? "#4f46e5" : "#e5e7eb",
              color: msg.senderId === userEmail ? "#fff" : "#111827",
            }}
          >
            <b>{msg.senderId.split("@")[0]}</b>: {msg.content}
          </div>
        ))}
        <div ref={chatEndRef}></div>
      </div>

      <div className="chat-input" style={styles.inputArea}>
        <input
          type="text"
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="Digite sua mensagem..."
          style={styles.input}
        />
        <button onClick={sendMessage} style={styles.button}>
          Enviar
        </button>
      </div>
    </div>
  );
};

// 🎨 Estilo inline
const styles = {
  container: {
    display: "flex",
    flexDirection: "column",
    height: "90vh",
    padding: "20px",
    backgroundColor: "#f3f4f6",
  },
  chatBox: {
    flex: 1,
    overflowY: "auto",
    display: "flex",
    flexDirection: "column",
    gap: "10px",
    padding: "10px",
    backgroundColor: "#fff",
    borderRadius: "10px",
    boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
  },
  message: {
    maxWidth: "70%",
    padding: "10px 14px",
    borderRadius: "12px",
    fontSize: "15px",
    lineHeight: "1.4",
  },
  inputArea: {
    marginTop: "15px",
    display: "flex",
    gap: "10px",
  },
  input: {
    flex: 1,
    padding: "10px",
    borderRadius: "8px",
    border: "1px solid #ccc",
    fontSize: "15px",
  },
  button: {
    backgroundColor: "#4f46e5",
    color: "#fff",
    padding: "10px 18px",
    border: "none",
    borderRadius: "8px",
    cursor: "pointer",
  },
};

export default ChatPage;
