//
//  ViewController.swift
//  PitchDetectorPrototype
//
//  Created by Jack Marty on 12/6/19.
//  Copyright © 2019 Jack Marty. All rights reserved.
//

import UIKit
import AudioKit

class PitchViewController: UIViewController {

    @IBOutlet weak var Note: UILabel!
    @IBOutlet weak var targetNote: UILabel!
    @IBOutlet weak var timerLabel: UILabel!
    @IBOutlet weak var scoreLabel: UILabel!
    @IBOutlet weak var blueCircle: UIView!
    @IBOutlet weak var greenCircle: UIView!
    
    var seconds = 3
    var timer = Timer()
    var timerDidStart = false
    var scoreCtr = 0
    
    var myNotes = Notes()
    var playSoundFiles = PlaySound()
    var processTone = ProcessTone()
    
    var mic: AKMicrophone!
    var tracker: AKFrequencyTracker!
    var silence: AKBooster!
    
    var randomNote = Int.random(in: 0..<12)
    
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
        Note.center = CGPoint(x: screenWidth/2, y: screenHeight/2)
        blueCircle.center = CGPoint(x: screenWidth/2, y: screenHeight/2)
        greenCircle.center = CGPoint(x: screenWidth/2, y: screenHeight/2)
        
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
        
        let targetArray = myNotes.shiftArray(randomNote: randomNote)
        let screenHeightInt = Int(screenHeight)
        
        //If micophone heads a volume above this amplitude, continue
        if tracker.amplitude > 0.05 {
        
            //Output frequency text
            print("frequency = \(tracker.frequency)")
            print("amplitude = \(tracker.amplitude)")
            
            //Set measured values from ProcessTone class
            let frequency = processTone.getBaseFrequency(frequency: Float(tracker.frequency), targetArray: targetArray)
            let roundedFrequency = Float(String(format: "%.3f", frequency))
            let index = processTone.getMeasuredFreqIndex(targetArray: targetArray, roundedFrequency: roundedFrequency)
            let centAmountInt = processTone.getCents(roundedFrequency: roundedFrequency, targetArray: targetArray)
            let y = processTone.getYCoordinate(initialCoordinate: screenHeightInt/2, centAmountInt: centAmountInt, decrement: 200)
            
            //Start or Stop timer based off of cent value
            if abs(centAmountInt) <= 50 && timerDidStart == false {
                startTimer()
                timerDidStart = true
                
                UIView.animate(withDuration: 5.0, delay: 0.0, options: [],
                animations: {
                    self.blueCircle.alpha = 0
                },
                completion: nil)
                
            } else if abs(centAmountInt) > 50 {
                timer.invalidate()
                seconds = 3
                timerLabel.text = "\(seconds)"
                timerDidStart = false
                
                UIView.animate(withDuration: 0.01, delay: 0.0, options: [],
                animations: {
                    self.blueCircle.alpha = 1
                },
                completion: nil)
            }
            
            //Put UI in center on the x axis and offset y by measured value
            Note.center = CGPoint(x: screenWidth/2, y: CGFloat(y))
            blueCircle.center = CGPoint(x: screenWidth/2, y: CGFloat(y))
            greenCircle.center = CGPoint(x: screenWidth/2, y: CGFloat(y))
            
            //Find octave of measured note
            let octave = Int(log2f(Float(tracker.frequency) / frequency))
            Note.text = "\(targetArray[index].name)\(octave)"
        } else {
            timer.invalidate()
            seconds = 3
            timerLabel.text = "\(seconds)"
            timerDidStart = false
            
            UIView.animate(withDuration: 0.01, delay: 0.0, options: [],
            animations: {
                self.blueCircle.alpha = 1
            },
            completion: nil)
        }
        
    }
    
    //https://medium.com/ios-os-x-development/build-an-stopwatch-with-swift-3-0-c7040818a10f
    
    @objc func updateTimer() {
        if seconds == 0 {
            timer.invalidate()
            seconds = 3
            timerLabel.text = "\(seconds)"
            timerDidStart = false
            randomNote = Int.random(in: 0..<12)
            targetNote.text = myNotes.Notes[randomNote].name
            scoreCtr += 1
            scoreLabel.text = "\(scoreCtr) pts"
        }
        seconds -= 1     //This will decrement(count down)the seconds.
        timerLabel.text = "\(seconds)" //This will update the label.
        
    }
    func startTimer() {
        timer = Timer.scheduledTimer(timeInterval: 1, target: self,   selector: (#selector(PitchViewController.updateTimer)), userInfo: nil, repeats: true)
    }
}

