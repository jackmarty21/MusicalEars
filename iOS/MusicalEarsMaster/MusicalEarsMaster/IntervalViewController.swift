//
//  ViewController.swift
//  IntervalDetectorPrototype
//
//  Created by Jack Marty on 12/6/19.
//  Copyright © 2019 Jack Marty. All rights reserved.
//

import UIKit
import AudioKit
import AVFoundation

class IntervalViewController: UIViewController {

    
    @IBOutlet weak var targetNote: UILabel!
    @IBOutlet weak var intervalTargetNote: UILabel!
    @IBOutlet weak var Note: UILabel!
    @IBOutlet weak var scoreLabel: UILabel!
    @IBOutlet weak var timerLabel: UILabel!
    @IBOutlet weak var intervalTimerLabel: UILabel!
    @IBOutlet weak var blueCircle: UIView!
    @IBOutlet weak var greenCircle: UIView!
    
    var baseSeconds = 1
    var intervalSeconds = 3
    var baseTimerDidStart = false
    var intervalTimerDidStart = false
    var scoreCtr = 0
    var isIntervalTest = false
    
    var myNotes = Notes()
    var playSoundFiles = PlaySound()
    var processTone = ProcessTone()
    var baseTimer = Timer()
    var intervalTimer = Timer()
    
    var mic: AKMicrophone!
    var tracker: AKFrequencyTracker!
    var silence: AKBooster!
    
    var randomNote = Int.random(in: 0..<12)
    var randomInterval = Int.random(in: 1..<12)
    var randomUpDown = Int.random(in: 0..<2)
    
    //Screen Size
    let screenWidth  = UIScreen.main.fixedCoordinateSpace.bounds.width
    let screenHeight = UIScreen.main.fixedCoordinateSpace.bounds.height
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        
        do {
            try AudioKit.stop()
        } catch {
            print(error)
        }
        
        AKSettings.audioInputEnabled = true
        AKSettings.defaultToSpeaker = true
        AKSettings.useBluetooth = true
        
        mic = AKMicrophone()
        tracker = AKFrequencyTracker(mic)
        silence = AKBooster(tracker, gain: 0)
        
        targetNote.text = myNotes.Notes[randomNote].name
        Note.text = myNotes.Notes[randomNote].name
        Note.center = CGPoint(x: screenWidth/2, y: 1.8*screenHeight/2-40)
        blueCircle.center = CGPoint(x: screenWidth/2, y: 1.8*screenHeight/2-40)
        greenCircle.center = CGPoint(x: screenWidth/2, y: 1.8*screenHeight/2-40)
        
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

        AudioKit.output = silence
        do {
            try AudioKit.start()
        } catch {
            AKLog("AudioKit did not start!")
        }
        Timer.scheduledTimer(timeInterval: 0.05,
                             target: self,
                             selector: #selector(PitchViewController.updateUI),
                             userInfo: nil,
                             repeats: true)
    }
    
    @IBAction func playAudio(_ sender: Any) {
        playSoundFiles.playSound(fileName: String(randomNote))
    }

    @objc func updateUI() {
        
        let baseTargetArray = myNotes.shiftArray(randomNote: randomNote)
        let intervalTargetArray = myNotes.shiftArray(randomNote: randomNote+randomInterval)
        intervalTargetNote.text = intervalTargetArray[5].name
        let screenHeightInt = Int(screenHeight)
        
        //If micophone heads a volume above this amplitude, continue
        if tracker.amplitude > 0.03 {
            
            //Output frequency text
            print("randomInterval = \(randomInterval)")
            print("frequency = \(tracker.frequency)")
            print("amplitude = \(tracker.amplitude)")
            
            let frequency : Float
            let roundedFrequency : Float?
            let index : Int
            let centAmountInt : Int
            let y : Int
            
            if isIntervalTest == true {
                //Set measured values from ProcessTone class
                frequency = processTone.getBaseFrequency(frequency: Float(tracker.frequency), targetArray: intervalTargetArray)
                roundedFrequency = Float(String(format: "%.3f", frequency))
                index = processTone.getMeasuredFreqIndex(targetArray: intervalTargetArray, roundedFrequency: roundedFrequency)
                centAmountInt = processTone.getCents(roundedFrequency: roundedFrequency, targetArray: intervalTargetArray)
                y = processTone.getYCoordinate(initialCoordinate: screenHeightInt/2, centAmountInt: centAmountInt, decrement: 100)
                
                //Start or Stop timer based off of cent value
                if abs(centAmountInt) <= 50 && intervalTimerDidStart == false {
                    startIntervalTimer()
                    intervalTimerDidStart = true
                    
                    UIView.animate(withDuration: 5.0, delay: 0.0, options: [],
                    animations: {
                        self.blueCircle.alpha = 0
                    },
                    completion: nil)
                    
                } else if abs(centAmountInt) > 50 {
                    intervalTimer.invalidate()
                    
                    intervalTimerDidStart = false
                    
                    UIView.animate(withDuration: 0.01, delay: 0.0, options: [],
                    animations: {
                        self.blueCircle.alpha = 1
                    },
                    completion: nil)
                }
                //Find octave of measured note
                let octave = Int(log2f(Float(tracker.frequency) / frequency))
                Note.text = "\(intervalTargetArray[index].name)\(octave)"
            } else {
                //Set measured values from ProcessTone class
                frequency = processTone.getBaseFrequency(frequency: Float(tracker.frequency), targetArray: baseTargetArray)
                roundedFrequency = Float(String(format: "%.3f", frequency))
               index = processTone.getMeasuredFreqIndex(targetArray: baseTargetArray, roundedFrequency: roundedFrequency)
                centAmountInt = processTone.getCents(roundedFrequency: roundedFrequency, targetArray: baseTargetArray)
                y = processTone.getYCoordinate(initialCoordinate: Int(1.8*screenHeightInt/2-40), centAmountInt: centAmountInt, decrement: 100)
                
                //Start or Stop timer based off of cent value
                if abs(centAmountInt) <= 50 && baseTimerDidStart == false {
                    startBaseTimer()
                    baseTimerDidStart = true
                    
                    UIView.animate(withDuration: 5.0, delay: 0.0, options: [],
                    animations: {
                        self.blueCircle.alpha = 0
                    },
                    completion: nil)
                    
                } else if abs(centAmountInt) > 50 {
                    baseTimer.invalidate()
                    baseSeconds = 1
                    timerLabel.text = "\(baseSeconds)"
                    baseTimerDidStart = false
                    
                    UIView.animate(withDuration: 0.01, delay: 0.0, options: [],
                    animations: {
                        self.blueCircle.alpha = 1
                    },
                    completion: nil)
                }
                //Find octave of measured note
                let octave = Int(log2f(Float(tracker.frequency) / frequency))
                Note.text = "\(baseTargetArray[index].name)\(octave)"
            }
            
            //Put UI in center on the x axis and offset y by measured value
            Note.center = CGPoint(x: screenWidth/2, y: CGFloat(y))
            blueCircle.center = CGPoint(x: screenWidth/2, y: CGFloat(y))
            greenCircle.center = CGPoint(x: screenWidth/2, y: CGFloat(y))
        } else {
            baseTimer.invalidate()
            baseSeconds = 1
            timerLabel.text = "\(baseSeconds)"
            baseTimerDidStart = false
            
            UIView.animate(withDuration: 0.01, delay: 0.0, options: [],
            animations: {
                self.blueCircle.alpha = 1
            },
            completion: nil)
        }
        
    }
    
    //https://medium.com/ios-os-x-development/build-an-stopwatch-with-swift-3-0-c7040818a10f
    
    @objc func updateBaseTimer() {
        //user got base note correct
        if baseSeconds == 0 && isIntervalTest == false {
            baseTimer.invalidate()
            baseSeconds = 10
            timerLabel.text = "\(baseSeconds)"
            baseTimerDidStart = false
            isIntervalTest = true
            startBaseTimer()
        }
        //User did not get target note, restart
        else if baseSeconds == 0 && isIntervalTest == true {
            baseTimer.invalidate()
            intervalTimer.invalidate()
            baseSeconds = 1
            timerLabel.text = "\(baseSeconds)"
            intervalSeconds = 3
            intervalTimerLabel.text = "\(intervalSeconds)"
            baseTimerDidStart = false
            intervalTimerDidStart = false
            isIntervalTest = false
        }
        baseSeconds -= 1     //This will decrement(count down)the seconds.
        timerLabel.text = "\(baseSeconds)" //This will update the label.
        
    }
    @objc func updateIntervalTimer() {
        //user did get target note correct
        if intervalSeconds == 0 {
            baseTimer.invalidate()
            intervalTimer.invalidate()
            baseSeconds = 1
            timerLabel.text = "\(baseSeconds)"
            intervalSeconds = 3
            intervalTimerLabel.text = "\(intervalSeconds)"
            baseTimerDidStart = false
            intervalTimerDidStart = false
            isIntervalTest = false
            randomNote = Int.random(in: 0..<12)
            randomInterval = Int.random(in: 1..<12)
            targetNote.text = myNotes.Notes[randomNote].name
            scoreCtr += 1
            scoreLabel.text = "\(scoreCtr) pts"
        }
        intervalSeconds -= 1     //This will decrement(count down)the seconds.
        intervalTimerLabel.text = "\(intervalSeconds)"
    }
    func startBaseTimer() {
        baseTimer = Timer.scheduledTimer(timeInterval: 1, target: self,   selector: (#selector(IntervalViewController.updateBaseTimer)), userInfo: nil, repeats: true)
    }
    func startIntervalTimer() {
        intervalTimer = Timer.scheduledTimer(timeInterval: 1, target: self,   selector: (#selector(IntervalViewController.updateIntervalTimer)), userInfo: nil, repeats: true)
    }
}

