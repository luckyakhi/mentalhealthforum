import { Link } from 'react-router-dom';
import { useGoogleLogin } from '@react-oauth/google';
import { useState } from 'react';
import './Navbar.css';

const Navbar = () => {
    const [user, setUser] = useState<any>(null);

    const login = useGoogleLogin({
        onSuccess: async (tokenResponse) => {
            try {
                const response = await fetch('/api/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ accessToken: tokenResponse.access_token }),
                });

                if (response.ok) {
                    const userData = await response.json();
                    setUser(userData);
                    console.log('User registered/logged in:', userData);
                } else {
                    console.error('Registration failed');
                }
            } catch (error) {
                console.error('Error connecting to backend:', error);
            }
        },
    });

    return (
        <nav className="navbar">
            <div className="container navbar-content">
                <Link to="/" className="logo">
                    AI Panic Relief
                </Link>
                <div className="nav-links">
                    <Link to="/forums">Forums</Link>
                    <Link to="/therapy">AI Therapy</Link>
                    <Link to="/human-therapy">Human Therapy</Link>
                </div>
                <div className="auth-buttons">
                    {user ? (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            {user.profilePictureUrl && (
                                <img
                                    src={user.profilePictureUrl}
                                    alt="Profile"
                                    style={{ width: '32px', height: '32px', borderRadius: '50%' }}
                                />
                            )}
                            <span style={{ color: 'var(--secondary-color)', fontWeight: '600' }}>{user.name}</span>
                        </div>
                    ) : (
                        <>
                            <button className="btn btn-secondary">Login</button>
                            <button className="btn btn-primary" onClick={() => login()}>
                                Sign Up with Google
                            </button>
                        </>
                    )}
                </div>
            </div>
        </nav>
    );
};

export default Navbar;
