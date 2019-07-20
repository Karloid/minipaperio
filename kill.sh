#!/bin/bash
ps aux | grep -ie localrunner | awk '{print $2}' | xargs kill -9 || (true)
