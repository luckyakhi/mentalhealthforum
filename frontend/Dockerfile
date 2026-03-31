FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install --legacy-peer-deps
COPY . .
ARG REACT_APP_USER_SERVICE_URL=http://localhost:30081
ARG REACT_APP_FORUM_SERVICE_URL=http://localhost:30082
ENV REACT_APP_USER_SERVICE_URL=$REACT_APP_USER_SERVICE_URL
ENV REACT_APP_FORUM_SERVICE_URL=$REACT_APP_FORUM_SERVICE_URL
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
