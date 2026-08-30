import React, { useState, useRef, useEffect } from 'react';
import { Sparkles, X, Send, Bot } from 'lucide-react';
import { governanceApi } from '../api/governanceApi';

export const AiChatWidget = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [input, setInput] = useState('');
  
  // Persistent memory state loaded from sessionStorage
  const [messages, setMessages] = useState(() => {
    const saved = sessionStorage.getItem('fairprice_ai_chat_history');
    if (saved) {
      try { return JSON.parse(saved); } catch (e) {}
    }
    return [
      {
        sender: 'bot',
        text: '👋 Hello! I am your **FairPrice AI Assistant**.\n\nAsk me about prices, fairness, market trends, complaints, or forecasts. For example:\n• *"What is the current price of Tomato in Coimbatore?"*\n• *"Is ₹85 fair for Toor Dal in Chennai?"*\n• *"What will tomato cost next week?"*',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      }
    ];
  });

  const [isTyping, setIsTyping] = useState(false);
  const [activeStatus, setActiveStatus] = useState('');
  const [userContext, setUserContext] = useState({
    lastLocation: 'Coimbatore',
    lastState: 'Tamil Nadu'
  });

  const chatEndRef = useRef(null);

  const suggestionChips = [
    "What is today's price of Tomato in Coimbatore?",
    "Is ₹85/kg fair for Toor Dal in Chennai?",
    "Show my reported price gouging complaints",
    "What will tomato cost in Coimbatore next week?",
    "Compare regional rates for Tomato in Tamil Nadu"
  ];

  useEffect(() => {
    sessionStorage.setItem('fairprice_ai_chat_history', JSON.stringify(messages));
    if (isOpen) {
      chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages, isOpen]);

  const handleSendMessage = async (textToSend) => {
    const query = textToSend || input;
    if (!query.trim()) return;

    const userMsg = {
      sender: 'user',
      text: query,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    };

    const updatedHistory = [...messages, userMsg];
    setMessages(updatedHistory);
    if (!textToSend) setInput('');
    setIsTyping(true);
    setActiveStatus('Analyzing your request...');

    try {
      const aiResult = await governanceApi.sendAiChatQuery(
        query,
        userContext.lastLocation,
        userContext.lastState,
        updatedHistory,
        'AI'
      );
      
      if (aiResult.detectedLocation) {
        setUserContext(prev => ({ ...prev, lastLocation: aiResult.detectedLocation }));
      }

      const botReply = {
        sender: 'bot',
        text: aiResult.reply || 'I have evaluated your request against live market benchmarks.',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        friendlyToolStatus: aiResult.friendlyToolStatus
      };

      setMessages(prev => [...prev, botReply]);
    } catch (e) {
      setMessages(prev => [
        ...prev,
        {
          sender: 'bot',
          text: 'AI assistance is temporarily unavailable. Please try again later.',
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
          isError: true
        }
      ]);
    } finally {
      setIsTyping(false);
      setActiveStatus('');
    }
  };

  const clearMemory = () => {
    sessionStorage.removeItem('fairprice_ai_chat_history');
    setMessages([
      {
        sender: 'bot',
        text: '🧹 Conversation history reset! How can I assist you now with live market rates, price fairness, or forecasts?',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      }
    ]);
  };

  return (
    <>
      {/* Floating Action Button */}
      {!isOpen && (
        <button
          onClick={() => setIsOpen(true)}
          style={{
            position: 'fixed',
            bottom: '32px',
            right: '32px',
            backgroundColor: '#166534',
            color: '#ffffff',
            border: 'none',
            borderRadius: '30px',
            padding: '14px 22px',
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            fontWeight: 700,
            fontSize: '15px',
            boxShadow: '0 10px 25px rgba(22, 101, 52, 0.4)',
            cursor: 'pointer',
            zIndex: 99,
            transition: 'all 0.2s ease'
          }}
        >
          <Sparkles size={20} color="#86efac" />
          <span>Ask FairPrice AI</span>
        </button>
      )}

      {/* Chat Drawer Widget */}
      {isOpen && (
        <div style={{
          position: 'fixed',
          bottom: '32px',
          right: '32px',
          width: '460px',
          height: '620px',
          backgroundColor: '#ffffff',
          borderRadius: '20px',
          boxShadow: '0 20px 40px rgba(0, 0, 0, 0.18)',
          border: '1px solid #e2e8f0',
          display: 'flex',
          flexDirection: 'column',
          zIndex: 100,
          overflow: 'hidden'
        }}>
          {/* Header */}
          <div style={{
            backgroundColor: '#166534',
            color: '#ffffff',
            padding: '16px 20px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <div style={{
                width: '38px',
                height: '38px',
                borderRadius: '50%',
                backgroundColor: 'rgba(255, 255, 255, 0.2)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}>
                <Sparkles size={20} color="#86efac" />
              </div>
              <div>
                <h4 style={{ fontSize: '16px', fontWeight: 700, color: '#ffffff', margin: 0 }}>FairPrice AI Assistant</h4>
                <p style={{ fontSize: '12px', color: '#bbf7d0', margin: 0 }}>
                  AI-powered price, fairness, market and complaint assistance
                </p>
              </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <button
                onClick={clearMemory}
                title="Reset Conversation Memory"
                style={{ background: 'none', border: 'none', color: '#bbf7d0', cursor: 'pointer', fontSize: '12px', fontWeight: 600 }}
              >
                Clear
              </button>
              <button
                onClick={() => setIsOpen(false)}
                style={{ background: 'none', border: 'none', color: '#ffffff', cursor: 'pointer' }}
              >
                <X size={20} />
              </button>
            </div>
          </div>

          {/* Messages Body */}
          <div style={{ flex: 1, padding: '16px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '12px', backgroundColor: '#f8fafc' }}>
            {messages.map((msg, index) => (
              <div
                key={index}
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: msg.sender === 'user' ? 'flex-end' : 'flex-start',
                  gap: '4px'
                }}
              >
                {/* Temporary Friendly Tool Status Badge */}
                {msg.friendlyToolStatus && (
                  <div style={{
                    fontSize: '11px',
                    color: '#0284c7',
                    backgroundColor: '#e0f2fe',
                    padding: '4px 10px',
                    borderRadius: '12px',
                    fontWeight: 600,
                    marginBottom: '2px',
                    border: '1px solid #bae6fd',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px'
                  }}>
                    ⚙️ {msg.friendlyToolStatus}
                  </div>
                )}

                <div style={{ display: 'flex', gap: '8px', maxWidth: '88%' }}>
                  {msg.sender === 'bot' && (
                    <div style={{
                      width: '28px',
                      height: '28px',
                      borderRadius: '50%',
                      backgroundColor: msg.isError ? '#dc2626' : '#166534',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      color: '#ffffff',
                      flexShrink: 0
                    }}>
                      <Bot size={16} />
                    </div>
                  )}

                  <div style={{
                    padding: '12px 14px',
                    borderRadius: '14px',
                    fontSize: '13px',
                    lineHeight: '1.5',
                    whiteSpace: 'pre-line',
                    backgroundColor: msg.sender === 'user' ? '#166534' : (msg.isError ? '#fef2f2' : '#ffffff'),
                    color: msg.sender === 'user' ? '#ffffff' : (msg.isError ? '#991b1b' : '#1e293b'),
                    border: msg.sender === 'user' ? 'none' : (msg.isError ? '1px solid #fecaca' : '1px solid #e2e8f0'),
                    boxShadow: '0 2px 6px rgba(0, 0, 0, 0.02)'
                  }}>
                    {msg.text}
                    <div style={{
                      fontSize: '10px',
                      color: msg.sender === 'user' ? '#bbf7d0' : '#94a3b8',
                      marginTop: '4px',
                      textAlign: 'right'
                    }}>
                      {msg.timestamp}
                    </div>
                  </div>
                </div>
              </div>
            ))}

            {isTyping && (
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#64748b', fontSize: '13px', padding: '4px 8px' }}>
                <Bot size={16} color="#166534" />
                <span>{activeStatus || 'FairPrice AI Assistant is thinking...'}</span>
              </div>
            )}
            <div ref={chatEndRef} />
          </div>

          {/* Quick Suggestions */}
          <div style={{ padding: '8px 12px', backgroundColor: '#ffffff', borderTop: '1px solid #f1f5f9', display: 'flex', gap: '6px', overflowX: 'auto' }}>
            {suggestionChips.map((chip, idx) => (
              <button
                key={idx}
                onClick={() => handleSendMessage(chip)}
                style={{
                  whiteSpace: 'nowrap',
                  padding: '6px 10px',
                  borderRadius: '12px',
                  backgroundColor: '#f0fdf4',
                  color: '#166534',
                  border: '1px solid #bbf7d0',
                  fontSize: '11px',
                  fontWeight: 600,
                  cursor: 'pointer'
                }}
              >
                {chip}
              </button>
            ))}
          </div>

          {/* Input Box */}
          <div style={{ padding: '12px', backgroundColor: '#ffffff', borderTop: '1px solid #e2e8f0', display: 'flex', gap: '8px' }}>
            <input
              type="text"
              placeholder="Ask about prices, fairness, market trends, complaints, or forecasts..."
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSendMessage()}
              style={{
                flex: 1,
                padding: '10px 14px',
                borderRadius: '10px',
                border: '1px solid #cbd5e1',
                fontSize: '13px',
                outline: 'none'
              }}
            />
            <button
              onClick={() => handleSendMessage()}
              style={{
                backgroundColor: '#166534',
                color: '#ffffff',
                border: 'none',
                borderRadius: '10px',
                padding: '0 16px',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
            >
              <Send size={16} />
            </button>
          </div>
        </div>
      )}
    </>
  );
};
