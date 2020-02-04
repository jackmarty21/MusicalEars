//
//  ViewController.swift
//  PitchDetectorPrototype
//
//  Created by Jack Marty on 12/6/19.
//  Copyright © 2019 Jack Marty. All rights reserved.
//

import UIKit
import AudioKit
import AVFoundation

class ViewController: UIViewController {

//    @IBOutlet weak var frequency: UILabel!
//    @IBOutlet weak var amplitude: UILabel!
    @IBOutlet weak var Note: UILabel!
    @IBOutlet weak var targetNote: UILabel!
    @IBOutlet weak var timerLabel: UILabel!
    @IBOutlet weak var scoreLabel: UILabel!
    @IBOutlet weak var blueCircle: UIView!
    @IBOutlet weak var greenCircle: UIView!
    
    var seconds = 3
    var timer = Timer()
    var isTimerRunning = false
    var timerDidStart = false
    var scoreCtr = 0
    
    var audioPlayer = AVAudioPlayer()
    
    var mic: AKMicrophone!
    var tracker: AKFrequencyTracker!
    var silence: AKBooster!
    
    var randomNote = Int.random(in: 0..<12)
    
    let noteFrequencies = [16.35, 17.32, 18.35, 19.45, 20.6, 21.83, 23.12, 24.5, 25.96, 27.5, 29.14, 30.87]
    let noteNamesWithSharps = ["C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B"]
    let noteNamesWithFlats = ["C", "D♭", "D", "E♭", "E", "F", "G♭", "G", "A♭", "A", "B♭", "B"]
    
    
    //Screen Size
    let screenWidth  = UIScreen.main.fixedCoordinateSpace.bounds.width
    let screenHeight = UIScreen.main.fixedCoordinateSpace.bounds.height
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        
        AKSettings.audioInputEnabled = true
        mic = AKMicrophone()
        tracker = AKFrequencyTracker(mic)
        silence = AKBooster(tracker, gain: 0)
        
        targetNote.text = noteNamesWithSharps[randomNote]
        Note.text = noteNamesWithSharps[randomNote]
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
                             selector: #selector(ViewController.updateUI),
                             userInfo: nil,
                             repeats: true)
    }
    
    @IBAction func playAudio(_ sender: Any) {
        playSound(fileName: String(randomNote))
    }

    @objc func updateUI() {
        
        let targetArrayFrequencies = createNewDoubleArray(array: noteFrequencies)
        let targetArrayNoteNames = createNewStringArray(array: noteNamesWithSharps)
        
        let screenHeightInt = Int(screenHeight)
        
        //If micophone heads a volume above this amplitude, continue
        if tracker.amplitude > 0.1 {
            
//            print(noteFrequencies)
//            print(targetArrayFrequencies)
//            print(noteNamesWithSharps)
//            print(targetArrayNoteNames)
            
            //Output frequency text
//            frequency.text = String(format: "%0.1f", tracker.frequency)
            print("frequency = \(tracker.frequency)")
            print("amplitude = \(tracker.amplitude)")
            
            //Divide frequency until base value to identify
            var frequency = Float(tracker.frequency)
            while frequency > Float(targetArrayFrequencies[targetArrayFrequencies.count - 1]) {
                frequency /= 2.0
            }
            while frequency < Float(targetArrayFrequencies[0]) {
                frequency *= 2.0
            }
            let roundedFrequency = Float(String(format: "%.3f", frequency))

            //distance = measured-target
            var distance: Float = 10_000.0
            var centValue: Float = 0.0
            var centAmount: Float = 0.0
            var centAmountInt: Int = 0
            var y = screenHeightInt/2
            
            //minDistance identifies measured frequency
            var minDistance: Float = 10_000.0
            var index = 0
            
            //Identify the measured frequency note
            for i in 0..<targetArrayFrequencies.count {
                let distanceTest = fabsf(Float(targetArrayFrequencies[i]) - roundedFrequency!)
                if distanceTest < minDistance {
                    index = i
                    minDistance = distanceTest
                }
            }
            
            
            distance = fabsf(Float(targetArrayFrequencies[5]) - roundedFrequency!)
            
            //if measured frequency is greater than target note, move UI up
            if roundedFrequency! > Float(targetArrayFrequencies[5]) {
                
                centValue = Float((targetArrayFrequencies[6]-targetArrayFrequencies[5])/100)
                
                centAmount = distance / Float(centValue)
                centAmountInt = Int(centAmount)
                
                if centAmountInt < 200 {
                    y = y - centAmountInt
                } else {
                    y = y - 200
                }
                
            }
            //if measured frequency is less than target note, move UI down
            else if roundedFrequency! < Float(targetArrayFrequencies[5]) {
                centValue = Float((targetArrayFrequencies[5]-targetArrayFrequencies[4])/100)
                
                centAmount = distance / Float(centValue)
                centAmountInt = Int(centAmount)
                
                if centAmountInt < 200 {
                    y = y + centAmountInt
                } else {
                    y = y + 200
                }
            }
            //If measured frequency is equal to target, then UI goes in the middle of the screen
            else {
                centAmountInt = 0
                y = screenHeightInt/2

            }
            print("cents = \(centAmountInt)")
            //Start or Stop timer based off of cent value
            if centAmountInt <= 50 && timerDidStart == false {
                startTimer()
                timerDidStart = true
                
                UIView.animate(withDuration: 5.0, delay: 0.0, options: [],
                animations: {
                    self.blueCircle.alpha = 0
                },
                completion: nil)
                
            } else if centAmountInt > 50{
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
            Note.text = "\(targetArrayNoteNames[index])\(octave)"
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
        
        //amplitude.text = String(format: "%0.2f", tracker.amplitude)
    }
    
    //Shift noteNames array left or right
    func createNewStringArray(array: Array<String>) -> Array<String>{
        
        var indexDiff = randomNote - 5
        var arr = array
                
        //if positive, shift left
        while (indexDiff > 0) {
            let first = arr[0]
            for i in 0..<arr.count - 1 {
                arr[i] = arr[i + 1]
            }
            arr[arr.count - 1] = first
            
            indexDiff -= 1
        }
            
        //if negative, shift right
        while (indexDiff < 0) {
            let last = arr[arr.count - 1]
            for i in (1..<arr.count).reversed() {
                arr[i] = arr[i - 1]
            }
            arr[0] = last
            indexDiff += 1
        }
        
        return arr
        
    }
    //Shift noteFrequencies array left or right and have frequencies from greatest to smallest
    func createNewDoubleArray(array: Array<Double>) -> Array<Double>{
        
        var indexDiff = randomNote - 5
        var arr = array
                
        //if positive, shift left
        while (indexDiff > 0) {
            let first = arr[0]
            for i in 0..<arr.count - 1 {
                arr[i] = arr[i + 1]
            }
            arr[arr.count - 1] = first
            arr[arr.count - 1] *= 2
            
            indexDiff -= 1
        }
            
        //if negative, shift right
        while (indexDiff < 0) {
            let last = arr[arr.count - 1]
            for i in (1..<arr.count).reversed() {
                arr[i] = arr[i - 1]
            }
            arr[0] = last
            arr[0] /= 2
            indexDiff += 1
        }
        
        return arr
        
    }
    
    //Code to play sound
    //Referenced code from https://gist.github.com/cliff538/91b8f8bf818d836e1d9537081d02c580
    func playSound(fileName : String) {
        let sound = Bundle.main.url(forResource: fileName, withExtension: "wav")
        do {
            audioPlayer = try AVAudioPlayer(contentsOf: sound!)
        }
        catch {
            print(error)
        }
        audioPlayer.play()
    }
    //https://medium.com/ios-os-x-development/build-an-stopwatch-with-swift-3-0-c7040818a10f
    
    @objc func updateTimer() {
        if seconds == 0 {
            timer.invalidate()
            seconds = 3
            timerLabel.text = "\(seconds)"
            timerDidStart = false
            randomNote = Int.random(in: 0..<12)
            targetNote.text = noteNamesWithSharps[randomNote]
            scoreCtr += 1
            scoreLabel.text = "\(scoreCtr) pts"
        }
        seconds -= 1     //This will decrement(count down)the seconds.
        timerLabel.text = "\(seconds)" //This will update the label.
        
    }
    func startTimer() {
        timer = Timer.scheduledTimer(timeInterval: 1, target: self,   selector: (#selector(ViewController.updateTimer)), userInfo: nil, repeats: true)
    }
}

