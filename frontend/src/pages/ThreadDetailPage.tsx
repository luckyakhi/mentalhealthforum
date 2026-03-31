import React, { useState, useEffect, useCallback } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { getThread, deleteThread, Thread } from '../api/threads';
import { getComments, addComment, upvoteComment, deleteComment, Comment } from '../api/comments';
import { useAuth } from '../context/AuthContext';
import CommentItem from '../components/CommentItem';
import LoadingSpinner from '../components/LoadingSpinner';

const formatDate = (dateStr: string): string => {
  const date = new Date(dateStr);
  return date.toLocaleDateString('en-US', {
    year: 'numeric', month: 'long', day: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
};

const ThreadDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [thread, setThread] = useState<Thread | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [commentContent, setCommentContent] = useState('');
  const [replyToId, setReplyToId] = useState<string | null>(null);
  const [replyContent, setReplyContent] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [commentError, setCommentError] = useState<string | null>(null);

  const fetchData = useCallback(async () => {
    if (!id) return;
    try {
      const [threadRes, commentsRes] = await Promise.all([
        getThread(id),
        getComments(id),
      ]);
      setThread(threadRes.data);
      setComments(Array.isArray(commentsRes.data) ? commentsRes.data : []);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to load thread.';
      setError(message);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleAddComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!commentContent.trim() || !id) return;
    setSubmitting(true);
    setCommentError(null);
    try {
      await addComment(id, { content: commentContent.trim() });
      setCommentContent('');
      await fetchData();
    } catch (err: unknown) {
      setCommentError('Failed to post comment. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleAddReply = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!replyContent.trim() || !id || !replyToId) return;
    setSubmitting(true);
    setCommentError(null);
    try {
      await addComment(id, { content: replyContent.trim(), parentCommentId: replyToId });
      setReplyContent('');
      setReplyToId(null);
      await fetchData();
    } catch (err: unknown) {
      setCommentError('Failed to post reply. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleUpvote = async (commentId: string) => {
    try {
      await upvoteComment(commentId);
      await fetchData();
    } catch {
      // silently fail upvote
    }
  };

  const handleDeleteComment = async (commentId: string) => {
    if (!window.confirm('Are you sure you want to delete this comment?')) return;
    try {
      await deleteComment(commentId);
      setComments(prev => prev.filter(c => c.id !== commentId));
    } catch {
      alert('Failed to delete comment.');
    }
  };

  const handleDeleteThread = async () => {
    if (!thread || !window.confirm('Are you sure you want to delete this thread?')) return;
    try {
      await deleteThread(thread.id);
      navigate(`/category/${thread.categoryId}`);
    } catch {
      alert('Failed to delete thread.');
    }
  };

  if (loading) return <LoadingSpinner />;

  if (error || !thread) {
    return (
      <div className="page-container">
        <div className="alert alert-error">{error || 'Thread not found.'}</div>
        <Link to="/" className="btn btn-secondary">Back to Home</Link>
      </div>
    );
  }

  const isLocked = thread.status === 'LOCKED';
  const isOwner = user?.userId === thread.authorId;

  // Separate top-level comments and replies
  const topLevelComments = comments.filter(c => !c.parentCommentId);
  const getReplies = (commentId: string) => comments.filter(c => c.parentCommentId === commentId);

  return (
    <div className="page-container">
      <div className="breadcrumb">
        <Link to="/" className="breadcrumb-link">Home</Link>
        <span className="breadcrumb-sep"> / </span>
        <Link to={`/category/${thread.categoryId}`} className="breadcrumb-link">Category</Link>
        <span className="breadcrumb-sep"> / </span>
        <span>{thread.title}</span>
      </div>

      <article className="thread-detail">
        <div className="thread-detail-header">
          <div className="thread-detail-title-row">
            <h1 className="thread-detail-title">{thread.title}</h1>
            {isLocked && <span className="badge badge-locked">Locked</span>}
          </div>
          <div className="thread-detail-meta">
            <span>👤 {thread.authorDisplayName}</span>
            <span>🗓 {formatDate(thread.createdAt)}</span>
            <span>👁 {thread.viewCount} views</span>
            <span>💬 {thread.commentCount} comments</span>
          </div>
          {isOwner && (
            <button
              className="btn btn-danger btn-sm"
              onClick={handleDeleteThread}
            >
              Delete Thread
            </button>
          )}
        </div>

        <div className="thread-detail-content">
          {thread.content.split('\n').map((paragraph, idx) => (
            paragraph.trim() ? <p key={idx}>{paragraph}</p> : <br key={idx} />
          ))}
        </div>
      </article>

      <section className="comments-section">
        <h2 className="comments-title">
          {comments.length} {comments.length === 1 ? 'Comment' : 'Comments'}
        </h2>

        {topLevelComments.length === 0 && (
          <div className="empty-state">
            <p>No comments yet. Be the first to share your thoughts!</p>
          </div>
        )}

        {topLevelComments.map(comment => (
          <div key={comment.id} className="comment-thread">
            <CommentItem
              comment={comment}
              isOwner={user?.userId === comment.authorId}
              onDelete={handleDeleteComment}
              onUpvote={handleUpvote}
              onReply={!isLocked && user ? (parentId) => {
                setReplyToId(parentId === replyToId ? null : parentId);
                setReplyContent('');
              } : undefined}
            />

            {/* Nested replies */}
            {getReplies(comment.id).map(reply => (
              <CommentItem
                key={reply.id}
                comment={reply}
                isOwner={user?.userId === reply.authorId}
                onDelete={handleDeleteComment}
                onUpvote={handleUpvote}
              />
            ))}

            {/* Reply form */}
            {replyToId === comment.id && user && !isLocked && (
              <form className="reply-form" onSubmit={handleAddReply}>
                <textarea
                  className="form-textarea reply-textarea"
                  placeholder="Write your reply..."
                  value={replyContent}
                  onChange={e => setReplyContent(e.target.value)}
                  rows={3}
                  required
                />
                <div className="reply-form-actions">
                  <button
                    type="submit"
                    className="btn btn-primary btn-sm"
                    disabled={submitting || !replyContent.trim()}
                  >
                    {submitting ? 'Posting...' : 'Post Reply'}
                  </button>
                  <button
                    type="button"
                    className="btn btn-secondary btn-sm"
                    onClick={() => setReplyToId(null)}
                  >
                    Cancel
                  </button>
                </div>
              </form>
            )}
          </div>
        ))}

        {/* New comment form */}
        {user && !isLocked && (
          <div className="new-comment-form">
            <h3 className="new-comment-title">Add a Comment</h3>
            {commentError && (
              <div className="alert alert-error">{commentError}</div>
            )}
            <form onSubmit={handleAddComment}>
              <textarea
                className="form-textarea"
                placeholder="Share your thoughts, offer support, or ask a question..."
                value={commentContent}
                onChange={e => setCommentContent(e.target.value)}
                rows={4}
                required
              />
              <button
                type="submit"
                className="btn btn-primary"
                disabled={submitting || !commentContent.trim()}
              >
                {submitting ? 'Posting...' : 'Post Comment'}
              </button>
            </form>
          </div>
        )}

        {!user && (
          <div className="auth-prompt">
            <p>
              <Link to="/login" className="auth-link">Sign in</Link> to join the conversation.
            </p>
          </div>
        )}

        {isLocked && (
          <div className="alert alert-info">
            This thread is locked. No new comments can be added.
          </div>
        )}
      </section>
    </div>
  );
};

export default ThreadDetailPage;
