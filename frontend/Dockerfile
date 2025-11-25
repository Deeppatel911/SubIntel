# --- Stage 1: Build the React App ---
FROM node:20-alpine AS build
WORKDIR /app

COPY subIntel/package*.json ./
RUN npm ci

COPY subIntel/ .
RUN npm run build

# --- Stage 2: Serve with Nginx ---
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

RUN rm -rf ./*

COPY --from=build /app/dist .

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx","-g","daemon off;"]
