import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:incoming_call/incoming_call.dart';

void main() {
  const MethodChannel channel = MethodChannel('incoming_call');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await IncomingCall.platformVersion, '42');
  });
}
