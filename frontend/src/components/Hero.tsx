import './Hero.css';

const Hero = () => {
    return (
        <section className="hero">
            <div className="container hero-content">
                <h1>Find Calm in the Chaos</h1>
                <p>A safe space to share, connect, and find relief from anxiety with the help of AI and a supportive community.</p>
                <div className="hero-buttons">
                    <button className="btn btn-primary">Join the Community</button>
                    <button className="btn btn-secondary">Talk to AI</button>
                </div>
            </div>
        </section>
    );
};

export default Hero;
