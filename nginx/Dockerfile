FROM nginx:alpine
COPY nginx/nginx.conf /etc/nginx/nginx.conf
COPY enstud-frontend/dist /usr/share/nginx/html
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl -sf http://localhost:80/ || exit 1
EXPOSE 80
