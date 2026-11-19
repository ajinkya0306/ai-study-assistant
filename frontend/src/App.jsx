
import React, { useEffect, useState } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import {
  Bot,
  BookOpen,
  
  Plus,
  Send,
  UploadCloud,
  FileText,
  File,
  Search,
  Trash2,
  Sparkles,
  LogOut
} from 'lucide-react';

import Login from './components/Login';
import { apiRequest } from './services/api';

export default function App() {

  // Authentication states
  const [user, setUser] = useState(null);
  const [authLoading, setAuthLoading] = useState(true);

  // Chat history states
  const [conversations, setConversations] = useState([]);
  const [activeConversationId, setActiveConversationId] = useState(null);

  // Chat states
  const [messages, setMessages] = useState([
    {
      role: 'assistant',
      content: 'Hello! I am your AI Study Assistant. Ask me about Java, Spring Boot, or interview preparation.'
    }
  ]);

  const [input, setInput] = useState('');
  const [mode, setMode] = useState('chat');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Document upload states
  const [selectedFile, setSelectedFile] = useState(null);
  const [topic, setTopic] = useState('General');
  const [documents, setDocuments] = useState([]);
  const [uploading, setUploading] = useState(false);
  const [uploadMessage, setUploadMessage] = useState('');
  const [uploadError, setUploadError] = useState('');

  // RAG search states
  const [question, setQuestion] = useState('');
  const [ragAnswer, setRagAnswer] = useState('');
  const [searching, setSearching] = useState(false);
  const [searchError, setSearchError] = useState('');

  // ==========================================
  // LOAD CONVERSATION HISTORY
  // ==========================================

  async function loadConversations() {
    const data = await apiRequest('/api/conversations');
    setConversations(data);
  }

  // ==========================================
  // CHECK LOGIN SESSION
  // ==========================================

  useEffect(() => {

    async function checkSession() {

      const token = localStorage.getItem('token');

      if (!token) {
        setAuthLoading(false);
        return;
      }

      try {

        const profile = await apiRequest('/api/auth/me');

        setUser(profile);

        const history = await apiRequest('/api/conversations');

        setConversations(history);

      } catch (err) {

        localStorage.removeItem('token');
        setUser(null);

      } finally {

        setAuthLoading(false);

      }
    }

    checkSession();

  }, []);

  // ==========================================
  // LOGIN SUCCESS
  // ==========================================

  async function handleLogin(data) {

    setUser({
      id: data.userId,
      name: data.name,
      email: data.email
    });

    try {
      await loadConversations();
    } catch (err) {
      setError(err.message);
    }
  }

  // ==========================================
  // LOGOUT
  // ==========================================

  async function handleLogout() {

    try {

      await apiRequest('/api/auth/logout', {
        method: 'POST'
      });

    } catch (err) {

      console.error('Logout error:', err.message);

    } finally {

      localStorage.removeItem('token');

      setUser(null);
      setConversations([]);
      setActiveConversationId(null);

      setMessages([
        {
          role: 'assistant',
          content: 'Hello! I am your AI Study Assistant.'
        }
      ]);

      setInput('');
      setError('');

    }
  }

  // ==========================================
  // START NEW CHAT
  // ==========================================

  function startNewChat() {

    setActiveConversationId(null);

    setMessages([
      {
        role: 'assistant',
        content: 'New conversation started. What would you like to learn?'
      }
    ]);

    setMode('chat');
    setInput('');
    setError('');
  }

  // ==========================================
  // OPEN OLD CONVERSATION
  // ==========================================

  async function openConversation(id) {

    try {

      setError('');

      const data = await apiRequest(`/api/conversations/${id}`);

      setActiveConversationId(data.id);

      setMessages(
        data.messages.map(message => ({
          role: message.role.toLowerCase() === 'user'
            ? 'user'
            : 'assistant',
          content: message.content
        }))
      );

      setMode('chat');
      setInput('');

    } catch (err) {

      setError(err.message);

    }
  }

  // ==========================================
  // DELETE CONVERSATION
  // ==========================================

  async function deleteConversation(id) {

    const confirmed = window.confirm(
      'Are you sure you want to delete this conversation?'
    );

    if (!confirmed) return;

    try {

      await apiRequest(`/api/conversations/${id}`, {
        method: 'DELETE'
      });

      setConversations(previous =>
        previous.filter(item => item.id !== id)
      );

      if (activeConversationId === id) {
        startNewChat();
      }

    } catch (err) {

      setError(err.message);

    }
  }

  // ==========================================
  // UPLOAD PDF / TXT
  // ==========================================

  async function uploadDocument(event) {

    event.preventDefault();

    if (!selectedFile) {
      setUploadError('Please select a document.');
      return;
    }

    if (!topic.trim()) {
      setUploadError('Please enter a topic.');
      return;
    }

    if (selectedFile.size > 10 * 1024 * 1024) {
      setUploadError('File size must be below 10 MB.');
      return;
    }

    setUploading(true);
    setUploadError('');
    setUploadMessage('');

    try {

      const formData = new FormData();

      formData.append('file', selectedFile);
      formData.append('topic', topic.trim());

      const data = await apiRequest('/api/rag/upload', {
        method: 'POST',
        body: formData
      });

      const newDocument = {
        id: crypto.randomUUID(),
        name: selectedFile.name,
        topic: topic.trim(),
        size: selectedFile.size,
        type: selectedFile.name.toLowerCase().endsWith('.pdf')
          ? 'PDF'
          : 'TXT',
        uploadedAt: new Date().toLocaleString()
      };

      setDocuments(previous => [newDocument, ...previous]);

      setUploadMessage(
        data.message || 'Document uploaded successfully.'
      );

      setSelectedFile(null);

      event.target.reset();

    } catch (err) {

      setUploadError(err.message);

    } finally {

      setUploading(false);

    }
  }

  // ==========================================
  // SEARCH STUDY NOTES
  // ==========================================

  async function searchNotes(event) {

    event.preventDefault();

    if (!question.trim() || searching) return;

    setSearching(true);
    setSearchError('');
    setRagAnswer('');

    try {

      const data = await apiRequest('/api/rag/ask', {
        method: 'POST',
        body: JSON.stringify({
          question: question.trim()
        })
      });

      setRagAnswer(data.answer || 'No answer received.');

    } catch (err) {

      setSearchError(err.message);

    } finally {

      setSearching(false);

    }
  }

  // ==========================================
  // SEND CHAT / CALCULATOR MESSAGE
  // ==========================================

  async function sendMessage(event) {

    event.preventDefault();

    const text = input.trim();

    if (!text || loading) return;

    // Show user message immediately
    setMessages(previous => [
      ...previous,
      {
        role: 'user',
        content: text
      }
    ]);

    setInput('');
    setLoading(true);
    setError('');

    try {

      // AI CHAT
      if (mode === 'chat') {

        const data = await apiRequest('/api/chat', {
          method: 'POST',
          body: JSON.stringify({
            conversationId: activeConversationId,
            message: text
          })
        });

        // Store numeric MySQL conversation ID
        setActiveConversationId(data.conversationId);

        setMessages(previous => [
          ...previous,
          {
            role: 'assistant',
            content: data.reply || 'No response received.'
          }
        ]);

        // Refresh sidebar history
        await loadConversations();

      }

      

      

    } catch (err) {

      setError(err.message);

    } finally {

      setLoading(false);

    }
  }

  // ==========================================
  // FORMAT FILE SIZE
  // ==========================================

  function formatSize(bytes) {

    return bytes < 1024 * 1024
      ? (bytes / 1024).toFixed(1) + ' KB'
      : (bytes / (1024 * 1024)).toFixed(2) + ' MB';
  }

  // ==========================================
  // AUTHENTICATION LOADING
  // ==========================================

  if (authLoading) {
    return <div>Loading your AI Study Assistant...</div>;
  }

  // ==========================================
  // SHOW LOGIN PAGE
  // ==========================================

  if (!user) {
    return <Login onLogin={handleLogin} />;
  }

  // ==========================================
  // MAIN APPLICATION
  // ==========================================

  return (

    <div className="app-shell">

      {/* SIDEBAR */}

      <aside className="sidebar">

        {/* BRAND */}

        <div className="brand">

          <div className="brand-icon">
            <Bot size={22} />
          </div>

          <div>
            <strong>StudyAI</strong>
            <small>Local AI workspace</small>
          </div>

        </div>

        {/* USER PROFILE */}

        <div className="sidebar-user">

          <div className="user-info">
            <strong>{user.name}</strong>
            
          </div>

          <button
            className="logout-button"
            onClick={handleLogout}
            title="Logout"
          >
            <LogOut size={17} />
          </button>

        </div>

        {/* NEW CHAT */}

        <button className="new-chat" onClick={startNewChat}>
          <Plus size={17} />
          New conversation
        </button>

        {/* TOOLS */}

        <div className="side-label">TOOLS</div>

        <button
          className={mode === 'chat' ? 'nav active' : 'nav'}
          onClick={() => setMode('chat')}
        >
          <Bot size={17} />
          AI Chat
        </button>

        <button
          className={mode === 'rag' ? 'nav active' : 'nav'}
          onClick={() => setMode('rag')}
        >
          <BookOpen size={17} />
          Search Study Notes
        </button>

        

        {/* CHAT HISTORY */}

        <div className="history-section">

          <div className="side-label">
            CHAT HISTORY
          </div>

          <div className="history-list">

            {conversations.length === 0 ? (

              <p className="history-empty">
                No conversations yet
              </p>

            ) : (

              conversations.map(conversation => (

                <div
                  className={
                    activeConversationId === conversation.id
                      ? 'history-item active'
                      : 'history-item'
                  }
                  key={conversation.id}
                >

                  <button
                    className="history-title"
                    onClick={() => openConversation(conversation.id)}
                    title={conversation.title}
                  >
                    {conversation.title}
                  </button>

                  <button
                    className="history-delete"
                    onClick={() => deleteConversation(conversation.id)}
                    title="Delete conversation"
                  >
                    <Trash2 size={14} />
                  </button>

                </div>

              ))

            )}

          </div>

        </div>

        {/* SIDEBAR BOTTOM */}

        <div className="sidebar-bottom">

          <span className="status-dot"></span>
          Local Ollama connection

          <br />

          <small>Model runs on your machine</small>

        </div>

      </aside>

      {/* MAIN */}

      <main className="main">

        {/* TOP BAR */}

        <header className="topbar">

          <div>

            <span className="eyebrow">
              YOUR LEARNING COMPANION
            </span>

            <h1>
              {mode === 'chat'
                ? 'AI Chat'
                
                : 'Search Study Notes'
                }
            </h1>

          </div>

          <div className="model-pill">

            <span className="status-dot"></span>
            StudyAI

          </div>

        </header>

        {/* ERROR MESSAGE */}

        {error && (
          <div className="error">
            {error}
          </div>
        )}

        {/* ======================================
            STUDY NOTES PAGE
        ====================================== */}

        {mode === 'rag' ? (

          <section className="notes-page">

            {/* UPLOAD CARD */}

            <div className="notes-card">

              <div className="notes-card-heading">

                <div className="notes-icon">
                  <UploadCloud size={23} />
                </div>

                <div>
                  <h2>Upload Study Documents</h2>
                  <p>
                    Upload your PDF or TXT files to search them using AI.
                  </p>
                </div>

              </div>

              <form onSubmit={uploadDocument}>

                <label className="field-label">
                  Choose document
                </label>

                <input
                  type="file"
                  accept=".pdf,.txt"
                  className="notes-file-input"
                  onChange={event => {
                    setSelectedFile(event.target.files?.[0] || null);
                    setUploadMessage('');
                    setUploadError('');
                  }}
                />

                {selectedFile && (

                  <div className="selected-file">

                    <FileText size={18} />

                    <span>{selectedFile.name}</span>

                    <small>
                      {formatSize(selectedFile.size)}
                    </small>

                  </div>

                )}

                <label className="field-label">
                  Document topic
                </label>

                <input
                  type="text"
                  className="notes-topic-input"
                  value={topic}
                  onChange={event => setTopic(event.target.value)}
                  placeholder="Example: Java, Spring Boot, DSA"
                  maxLength={100}
                />

                <button
                  type="submit"
                  className="notes-upload-button"
                  disabled={!selectedFile || uploading}
                >
                  <UploadCloud size={17} />

                  {uploading
                    ? 'Uploading and indexing...'
                    : 'Upload Document'}
                </button>

              </form>

              {uploadMessage && (
                <div className="upload-success">
                  {uploadMessage}
                </div>
              )}

              {uploadError && (
                <div className="upload-error">
                  {uploadError}
                </div>
              )}

              <p className="notes-hint">
                Supported formats: PDF and TXT · Maximum file size: 10 MB
              </p>

            </div>

            {/* UPLOADED DOCUMENTS */}

            <div className="documents-section">

              <div className="section-heading">

                <div>
                  <h2>Uploaded Notes</h2>
                  <p>Your uploaded study documents</p>
                </div>

                <span className="document-count">
                  {documents.length} Documents
                </span>

              </div>

              {documents.length === 0 ? (

                <div className="empty-documents">

                  <File size={34} />

                  <h3>No documents uploaded yet</h3>

                  <p>
                    Upload your first PDF or TXT document to see it here.
                  </p>

                </div>

              ) : (

                <div className="documents-grid">

                  {documents.map(document => (

                    <div
                      className="document-card"
                      key={document.id}
                    >

                      <div className="document-card-top">

                        <div className="document-file-icon">
                          <FileText size={24} />
                        </div>

                        <span className="indexed-label">
                          Indexed
                        </span>

                      </div>

                      <h3 title={document.name}>
                        {document.name}
                      </h3>

                      <p className="document-topic">
                        {document.topic}
                      </p>

                      <div className="document-meta">
                        <span>{document.type}</span>
                        <span>{formatSize(document.size)}</span>
                      </div>

                      <div className="document-date">
                        Uploaded: {document.uploadedAt}
                      </div>

                    </div>

                  ))}

                </div>

              )}

            </div>

            {/* SEARCH NOTES */}

            <div className="notes-card search-notes-card">

              <div className="notes-card-heading">

                <div className="notes-icon">
                  <Search size={23} />
                </div>

                <div>
                  <h2>Ask Your Study Notes</h2>
                  <p>
                    Ask questions and get answers from indexed documents.
                  </p>
                </div>

              </div>

              <form
                onSubmit={searchNotes}
                className="notes-search-form"
              >

                <input
                  type="text"
                  value={question}
                  onChange={event => setQuestion(event.target.value)}
                  placeholder="Example: What are my Java technical skills?"
                />

                <button
                  type="submit"
                  disabled={!question.trim() || searching}
                >
                  <Search size={17} />

                  {searching ? 'Searching...' : 'Search Notes'}
                </button>

              </form>

              <div className="notes-suggestions">

                <button
                  type="button"
                  onClick={() => setQuestion(
                    'What are my technical skills and programming languages?'
                  )}
                >
                  Technical skills
                </button>

                <button
                  type="button"
                  onClick={() => setQuestion(
                    'Explain Java and Spring Boot from my notes.'
                  )}
                >
                  Java and Spring Boot
                </button>

              </div>

              {searchError && (
                <div className="upload-error">
                  {searchError}
                </div>
              )}

              {searching && (

                <div className="search-loading">
                  <Sparkles size={18} />
                  Searching your study documents...
                </div>

              )}

              {ragAnswer && (

                <div className="rag-answer">

                  <div className="answer-heading">
                    <Bot size={19} />
                    AI Answer
                  </div>

                  <div className="answer-content">
                    {ragAnswer}
                  </div>

                </div>

              )}

            </div>

          </section>

        ) : (

          /* ======================================
             AI CHAT AND CALCULATOR PAGES
          ====================================== */

          <>

            <section className="chat-area">

              {messages.length <= 1 && (

                <div className="welcome">

                  <div className="welcome-icon">
                    <Sparkles size={27} />
                  </div>

                  <h2>What are we learning today?</h2>

                  <p>
                    Ask a question, explore a topic, or prepare for your next interview.
                  </p>

                  <div className="suggestions">

                    {[
                      'Explain Java Collections simply',
                      'Give me 5 Spring Boot interview questions'
                    ].map((item) => (
                      <button
                        key={item}
                        onClick={() => setInput(item)}
                      >
                        {item}
                      </button>
                    ))}

                  </div>

                </div>

              )}

              <div className="messages">

                {messages.map((message, index) => (

                  <div
                    className={`message ${message.role}`}
                    key={index}
                  >

                    <div className="avatar">

                      {message.role === 'assistant'
                        ? <Bot size={18} />
                        : 'You'}

                    </div>

                    <div className="bubble">

                      <div className="message-label">
                        {message.role === 'assistant'
                          ? 'STUDY AI'
                          : 'YOU'}
                      </div>

                      <div className="message-content markdown-content">
                        <ReactMarkdown remarkPlugins={[remarkGfm]}>
                          {message.content || ''}
                        </ReactMarkdown>
                      </div>

                    </div>

                  </div>

                ))}

                {loading && (

                  <div className="message assistant">

                    <div className="avatar">
                      <Bot size={18} />
                    </div>

                    <div className="bubble">

                      <div className="message-label">
                        STUDY AI
                      </div>

                      <div className="typing">
                        Thinking...
                      </div>

                    </div>

                  </div>

                )}

              </div>

            </section>

            {/* MESSAGE COMPOSER */}

            <div className="composer-wrap">

              <form
                className="composer"
                onSubmit={sendMessage}
              >

                <input
                  value={input}
                  onChange={event => setInput(event.target.value)}
                  placeholder={
                    
                       'Message your AI study assistant...'
                  }
                />

                <button
                  type="submit"
                  disabled={!input.trim() || loading}
                >
                  <Send size={18} />
                </button>

              </form>

              <p className="disclaimer">
                AI can make mistakes. Verify important information.
                Your local Ollama model processes requests on this machine.
              </p>

            </div>

          </>

        )}

      </main>

    </div>
  );
}
