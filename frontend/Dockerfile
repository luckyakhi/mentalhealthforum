# Stage 1: The Build Stage
# Using a slim image which is compatible and includes necessary build tools.
FROM node:24-slim AS builder
WORKDIR /app

# Install dependencies
COPY package*.json ./
RUN npm install

# Copy source and build the app
COPY . .
RUN npm run build

# Stage 2: The Serve Stage
# Nginx is a lightweight server for our static files.
FROM nginx:stable-alpine

# Copy the build output from the builder stage.
# Vite builds to a 'dist' folder, not 'build'.
COPY --from=builder /app/dist /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]