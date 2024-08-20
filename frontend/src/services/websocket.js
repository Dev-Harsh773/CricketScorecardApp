import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

let stompClient = null;

export const connectToMatch = (matchKey, onMessage) => {
  const socket = new SockJS('http://localhost:8080/ws');
  stompClient = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    onConnect: () => {
      stompClient.subscribe(`/topic/match/${matchKey}/score`, (message) => {
        const data = JSON.parse(message.body);
        onMessage(data);
      });
    },
    onStompError: (frame) => {
      console.error('STOMP error:', frame);
    },
  });
  stompClient.activate();
  return stompClient;
};

export const disconnectFromMatch = () => {
  if (stompClient && stompClient.active) {
    stompClient.deactivate();
    stompClient = null;
  }
};
