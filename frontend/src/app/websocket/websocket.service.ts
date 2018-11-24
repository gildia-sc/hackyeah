import { Injectable, OnDestroy, OnInit } from '@angular/core';
import * as Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';

@Injectable()
export class WebsocketService implements OnInit, OnDestroy {

  stompClient: any;
  serverUrl = '/api/ws';

  subscriptions = new Map();
  connectionEstablished: Promise<boolean>;

  constructor() { }

  ngOnInit(): void {
    this.initializeConnection();
  }

  ngOnDestroy(): void {
    this.disconnectWebsocket();
  }

  private initializeConnection() {
    this.connectionEstablished = new Promise<boolean>((resolve, reject) => {
      this.connectToWebSocket(resolve);
    });
  }

  private connectToWebSocket(resolve) {
    const ws = new SockJS(this.serverUrl);
    this.stompClient = Stomp.over(ws);
    this.stompClient.connect({},
      (frame) => {
        resolve(true);
      },
      (frame) => {
        setTimeout(() => {
          this.connectToWebSocket(resolve);
        }, 30000);
      });
  }

  subscribeToChannel(channel: string, handler: (message: any) => any) {
    if (this.stompClient == null) {
      this.initializeConnection();
    }
    this.connectionEstablished.then(established => {
      if (established) {
        const subscription = this.stompClient.subscribe(channel, handler);
        this.subscriptions.set(channel, subscription);
      }
    });
  }

  unsubscribeFromChannel(channel: string) {
    const subscription = this.subscriptions.get(channel);
    if (subscription) {
      subscription.unsubscribe();
    }
  }

  disconnectWebsocket() {
    this.stompClient.disconnect();
    this.stompClient = null;
  }
}
