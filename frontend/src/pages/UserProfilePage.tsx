import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { getProfile, getMe, updateProfile, UserProfile } from '../api/users';
import { useAuth } from '../context/AuthContext';
import LoadingSpinner from '../components/LoadingSpinner';

const formatDate = (dateStr: string): string => {
  const date = new Date(dateStr);
  return date.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
};

const UserProfilePage: React.FC = () => {
  const { userId } = useParams<{ userId: string }>();
  const { user: authUser } = useAuth();

  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [editing, setEditing] = useState(false);
  const [editUsername, setEditUsername] = useState('');
  const [editBio, setEditBio] = useState('');
  const [editAvatarUrl, setEditAvatarUrl] = useState('');
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [saveSuccess, setSaveSuccess] = useState(false);

  const isOwnProfile = authUser?.userId === userId || userId === 'me';

  useEffect(() => {
    const fetchProfile = async () => {
      setLoading(true);
      setError(null);
      try {
        let res;
        if (isOwnProfile && authUser) {
          res = await getMe();
        } else if (userId) {
          res = await getProfile(userId);
        } else {
          setError('Invalid profile.');
          setLoading(false);
          return;
        }
        setProfile(res.data);
        setEditUsername(res.data.username);
        setEditBio(res.data.bio || '');
        setEditAvatarUrl(res.data.avatarUrl || '');
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : 'Failed to load profile.';
        setError(message);
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, [userId, isOwnProfile, authUser]);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaveError(null);
    setSaveSuccess(false);

    if (!editUsername.trim()) {
      setSaveError('Username cannot be empty.');
      return;
    }

    setSaving(true);
    try {
      const res = await updateProfile({
        username: editUsername.trim(),
        bio: editBio.trim(),
        avatarUrl: editAvatarUrl.trim(),
      });
      setProfile(res.data);
      setEditing(false);
      setSaveSuccess(true);
      setTimeout(() => setSaveSuccess(false), 3000);
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } };
      setSaveError(axiosError?.response?.data?.message || 'Failed to update profile.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <LoadingSpinner />;

  if (error || !profile) {
    return (
      <div className="page-container">
        <div className="alert alert-error">{error || 'Profile not found.'}</div>
      </div>
    );
  }

  const roleBadgeClass = profile.role === 'ADMIN' ? 'badge-admin' : profile.role === 'MODERATOR' ? 'badge-mod' : 'badge-user';

  return (
    <div className="page-container">
      <div className="profile-page">
        <div className="profile-header">
          <div className="profile-avatar">
            {profile.avatarUrl ? (
              <img src={profile.avatarUrl} alt={profile.username} className="avatar-img" />
            ) : (
              <div className="avatar-placeholder">
                {profile.username.charAt(0).toUpperCase()}
              </div>
            )}
          </div>
          <div className="profile-info">
            <h1 className="profile-username">{profile.username}</h1>
            <span className={`badge ${roleBadgeClass}`}>{profile.role}</span>
            <p className="profile-joined">Member since {formatDate(profile.createdAt)}</p>
            {profile.bio && <p className="profile-bio">{profile.bio}</p>}
          </div>
          {isOwnProfile && !editing && (
            <button
              className="btn btn-secondary"
              onClick={() => setEditing(true)}
            >
              Edit Profile
            </button>
          )}
        </div>

        {saveSuccess && (
          <div className="alert alert-success">Profile updated successfully!</div>
        )}

        {editing && isOwnProfile && (
          <div className="profile-edit-form">
            <h2 className="section-title">Edit Profile</h2>
            {saveError && <div className="alert alert-error">{saveError}</div>}
            <form onSubmit={handleSave}>
              <div className="form-group">
                <label htmlFor="editUsername" className="form-label">Username</label>
                <input
                  id="editUsername"
                  type="text"
                  className="form-input"
                  value={editUsername}
                  onChange={e => setEditUsername(e.target.value)}
                  required
                />
              </div>
              <div className="form-group">
                <label htmlFor="editBio" className="form-label">Bio</label>
                <textarea
                  id="editBio"
                  className="form-textarea"
                  placeholder="Tell the community a bit about yourself..."
                  value={editBio}
                  onChange={e => setEditBio(e.target.value)}
                  rows={4}
                />
              </div>
              <div className="form-group">
                <label htmlFor="editAvatarUrl" className="form-label">Avatar URL</label>
                <input
                  id="editAvatarUrl"
                  type="url"
                  className="form-input"
                  placeholder="https://example.com/your-avatar.jpg"
                  value={editAvatarUrl}
                  onChange={e => setEditAvatarUrl(e.target.value)}
                />
              </div>
              <div className="form-actions">
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Saving...' : 'Save Changes'}
                </button>
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => {
                    setEditing(false);
                    setSaveError(null);
                    setEditUsername(profile.username);
                    setEditBio(profile.bio || '');
                    setEditAvatarUrl(profile.avatarUrl || '');
                  }}
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        )}
      </div>
    </div>
  );
};

export default UserProfilePage;
