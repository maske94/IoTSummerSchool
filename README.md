# Android IoT

## Requirements

* Android ≥ 4.4 ([KITKAT](https://developer.android.com/reference/android/os/Build.VERSION_CODES.html#KITKAT)), with bluetooth capabilities
* Shimmer 3R
* [Git](https://git-scm.com/) (not strictly necessary)

## Goals

* Plot data coming from Shimmer 3R sensor
* Store data in a [CSV](https://en.wikipedia.org/wiki/Comma-separated_values) format

## If you are brave enough

1. Fork this repository (a [GitHub](https://github.com/join) account is needed)
2. Make some changes to your local files

When you have something working...
1. git add .
2. git commit -m "My commit message"
4. git push -u origin master --all

## If not...

1. [Download](https://github.com/igneg/Android-IoT/archive/master.zip) it!
2. Start making changes as you wish

## Useful classes

### Android IoT Project

| **File**          | **Description**                                               |
|-----------------	|--------------------------------------------------------------	|
| MainActivity    	| Starts the Shimmer service and handles fragments interactions |
| MyService       	| Handles connectivity with Shimmer 3R device                  	|
| BaseTabFragment 	| Base fragment class for a tab interface                      	|
| DataTabFragment 	| Display accelerometer data            						| 

### Shimmer Library

| **File**          | **Description**                                               |
|-----------------	|--------------------------------------------------------------	|
| Shimmer    		| Abstracts Shimmer 3R functionality 							|
| ObjectCluster     | Wraps Shimmer 3R data                							|

