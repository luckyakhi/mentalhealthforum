import './ForumList.css';

const mockForums = [
    { id: 1, title: "General Anxiety Support", activeUsers: 120, posts: 3400, description: "A place to discuss general anxiety and share coping strategies." },
    { id: 2, title: "Panic Attack Help", activeUsers: 85, posts: 1200, description: "Immediate support and tips for dealing with panic attacks." },
    { id: 3, title: "Social Anxiety", activeUsers: 95, posts: 2100, description: "Discussing challenges and triumphs in social situations." },
    { id: 4, title: "Medication & Therapy", activeUsers: 60, posts: 1500, description: "Sharing experiences with different treatments and therapies." },
];

const ForumList = () => {
    return (
        <section className="forum-list-section">
            <div className="container">
                <h2>Active Discussions</h2>
                <div className="forum-grid">
                    {mockForums.map((forum) => (
                        <div key={forum.id} className="forum-card">
                            <h3>{forum.title}</h3>
                            <p>{forum.description}</p>
                            <div className="forum-stats">
                                <span>{forum.activeUsers} online</span>
                                <span>{forum.posts} posts</span>
                            </div>
                            <button className="btn btn-secondary">View Forum</button>
                        </div>
                    ))}
                </div>
            </div>
        </section>
    );
};

export default ForumList;
