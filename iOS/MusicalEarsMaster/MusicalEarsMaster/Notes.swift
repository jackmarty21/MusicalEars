//
//  Notes.swift
//  MusicalEarsMaster
//
//  Created by Jack Marty on 2/12/20.
//  Copyright © 2020 Jack Marty. All rights reserved.
//

import Foundation

struct Note {
    var name : String
    var frequency : Float
}

class Notes {
    
    var Notes : [Note] = [
        
        Note(name: "C", frequency: 16.35),
        Note(name: "C#", frequency: 17.32),
        Note(name: "D", frequency: 18.35),
        Note(name: "D#", frequency: 19.45),
        Note(name: "E", frequency: 20.6),
        Note(name: "F", frequency: 21.83),
        Note(name: "F#", frequency: 23.12),
        Note(name: "G", frequency: 24.5),
        Note(name: "G#", frequency: 25.96),
        Note(name: "A", frequency: 27.5),
        Note(name: "A#", frequency: 29.14),
        Note(name: "B", frequency: 30.87)
    ]

    
    //Shift Notes array left or right and have frequencies from greatest to smallest
    func shiftArray(randomNote: Int) -> [Note]{
        
        var indexDiff = randomNote - 5
        var arr = Notes
                
        //if positive, shift left
        while (indexDiff > 0) {
            let first = arr[0]
            for i in 0..<arr.count - 1 {
                arr[i] = arr[i + 1]
            }
            arr[arr.count - 1] = first
            arr[arr.count - 1].frequency *= 2
            
            indexDiff -= 1
        }
            
        //if negative, shift right
        while (indexDiff < 0) {
            let last = arr[arr.count - 1]
            for i in (1..<arr.count).reversed() {
                arr[i] = arr[i - 1]
            }
            arr[0] = last
            arr[0].frequency /= 2
            indexDiff += 1
        }
        
        return arr
        
    }
    
}
