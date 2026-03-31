import React from 'react';
import { Link } from 'react-router-dom';
import { ThreadSummary } from '../api/threads';

interface ThreadCardProps {
  thread: ThreadSummary;
}

const formatDate = (dateStr: string): string => {
  const date = new Date(dateStr);
  return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
};

const ThreadCard: React.FC<ThreadCardProps> = ({ thread }) => {
  const isPinned = thread.status === 'PINNED';
  const isLocked = thread.status === 'LOCKED';

  return (
    <div className={`thread-card ${isPinned ? 'thread-card--pinned' : ''}`}>
      <div className="thread-card-header">
        <div className="thread-card-title-row">
          {isPinned && <span className="thread-pin-icon" title="Pinned">📌</span>}
          <Link to={`/thread/${thread.id}`} className="thread-card-title">
            {thread.title}
          </Link>
          {isLocked && <span className="badge badge-locked">Locked</span>}
          {isPinned && <span className="badge badge-pinned">Pinned</span>}
        </div>
        <div className="thread-card-meta">
          <span className="thread-card-author">
            <span className="meta-icon">👤</span> {thread.authorDisplayName}
          </span>
          <span className="thread-card-date">
            <span className="meta-icon">🗓</span> {formatDate(thread.createdAt)}
          </span>
          <span className="thread-card-views">
            <span className="meta-icon">👁</span> {thread.viewCount} views
          </span>
          <span className="thread-card-comments">
            <span className="meta-icon">💬</span> {thread.commentCount} comments
          </span>
        </div>
      </div>
    </div>
  );
};

export default ThreadCard;
