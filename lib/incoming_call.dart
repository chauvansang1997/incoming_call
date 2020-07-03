import 'package:flutter/services.dart';

class IncomingCall {
  static const MethodChannel _channel = const MethodChannel('incoming_call');

  static Function(String numberPhone) inComingCall;

  static void registerListener(Function(String numberPhone) listener) {
    inComingCall = listener;
    _channel.setMethodCallHandler((MethodCall call) async {
      if (call.method == 'phone.incoming') {
        inComingCall(call.arguments);
      }
    });
  }
}
