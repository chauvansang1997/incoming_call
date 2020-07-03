import 'package:flutter/material.dart';
import 'package:incoming_call/incoming_call.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String callNumber = '';

  @override
  void initState() {
    super.initState();
    IncomingCall.registerListener((numberPhone) {
      callNumber = numberPhone;
      setState(() {});
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('Running on: $callNumber\n'),
        ),
      ),
    );
  }
}
