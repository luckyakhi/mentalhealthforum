# Stage 1: Build
FROM node:24-slim AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
ARG VITE_USER_SERVICE_URL=http://localhost:30081
ARG VITE_FORUM_SERVICE_URL=http://localhost:30082
ENV VITE_USER_SERVICE_URL=$VITE_USER_SERVICE_URL
ENV VITE_FORUM_SERVICE_URL=$VITE_FORUM_SERVICE_URL
RUN npm run build

# Stage 2: Serve
FROM nginx:stable-alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
