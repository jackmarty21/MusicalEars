//
//  PlaySound.swift
//  MusicalEarsMaster
//
//  Created by Jack Marty on 2/12/20.
//  Copyright © 2020 Jack Marty. All rights reserved.
//

import Foundation
import AVFoundation

class PlaySound {
    
    var audioPlayer = AVAudioPlayer()
    
    //Code to play sound
    //Referenced code from https://gist.github.com/cliff538/91b8f8bf818d836e1d9537081d02c580
    func playSound(fileName : String) {

        let sound = Bundle.main.url(forResource: fileName, withExtension: "wav", subdirectory: "sounds")
        do {
            audioPlayer = try AVAudioPlayer(contentsOf: sound!)
        }
        catch {
            print(error)
        }
        audioPlayer.play()
    }
    
    
}
