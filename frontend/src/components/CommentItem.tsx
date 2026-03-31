import React from 'react';
import { Comment } from '../api/comments';

interface CommentItemProps {
  comment: Comment;
  isOwner: boolean;
  onDelete: (id: string) => void;
  onUpvote: (id: string) => void;
  onReply?: (parentId: string) => void;
}

const formatDate = (dateStr: string): string => {
  const date = new Date(dateStr);
  return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
};

const CommentItem: React.FC<CommentItemProps> = ({ comment, isOwner, onDelete, onUpvote, onReply }) => {
  return (
    <div className={`comment-item ${comment.parentCommentId ? 'comment-item--nested' : ''}`}>
      <div className="comment-header">
        <span className="comment-author">
          <span className="comment-avatar">👤</span>
          {comment.authorDisplayName}
        </span>
        <span className="comment-date">{formatDate(comment.createdAt)}</span>
      </div>
      <div className="comment-content">{comment.content}</div>
      <div className="comment-actions">
        <button
          className="comment-action-btn comment-upvote-btn"
          onClick={() => onUpvote(comment.id)}
          title="Upvote"
        >
          ▲ {comment.upvotes}
        </button>
        {onReply && (
          <button
            className="comment-action-btn comment-reply-btn"
            onClick={() => onReply(comment.id)}
          >
            Reply
          </button>
        )}
        {isOwner && (
          <button
            className="comment-action-btn comment-delete-btn"
            onClick={() => onDelete(comment.id)}
          >
            Delete
          </button>
        )}
      </div>
    </div>
  );
};

export default CommentItem;
