# -*- coding: utf-8 -*-

import pandas as pd
import torch
import torch.nn as nn
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
import torch.nn.functional as F
from torch import optim



xHarData = pd.read_csv('UCI HAR Dataset/UCI HAR Dataset/train/X_train.txt', header=None)
yHarData = pd.read_csv('UCI HAR Dataset/UCI HAR Dataset/train/y_train.txt', header=None)

#harData = xHarData
#xHarData['Activity'] = yHarData[0]

#print(harData)

#harData.Activity.value_counts().plot(kind='pie', autopct='%1.0f%%', colors=['skyblue', 'orange', 'red', 'green', 'yellow', 'maroon'], explode=(0.05, 0.05, 0.05, 0.05, 0.05, 0.05))

def transform(x):
 
    x = (x.split(' '))
    ans = [] 
    
    for i, data in enumerate(x):
        if not data == '':
            ans.append(float(data))
       
    return ans


newTrainData =[]

for i in range(len(xHarData[0])):
    newTrainData.append(transform(xHarData[0][i]))


X_train = torch.tensor(newTrainData)
#print(inputs)

y_train = torch.tensor(yHarData[0].values).flatten()


#print(outputs)

#print(inputs.shape)
#print(outputs.shape)

X_testData = pd.read_csv('UCI HAR Dataset/UCI HAR Dataset/test/X_test.txt', header=None)
y_testData = pd.read_csv('UCI HAR Dataset/UCI HAR Dataset/test/y_test.txt', header=None)

newTestData = []

for i in range(len(X_testData[0])):
    newTestData.append((transform(X_testData[0][i])))

X_test = torch.tensor(newTestData)
y_test = torch.tensor(y_testData[0].values).flatten()

#print(x_test.shape)
#print(y_test.shape)


class Network(nn.Module):
    def __init__(self):
        super().__init__()
        # Inputs to hidden layer linear transformation
        self.h1 = nn.Linear(561, 1000)
        self.h2 = nn.Linear(1000, 500)
              
        # Output layer, 10 units - one for each digit
        self.output = nn.Linear(500, 7)
        
    def forward(self, x):
               
        # Hidden layer with sigmoid activation
        x = F.dropout(F.relu(self.h1(x)))
        x = F.dropout(F.relu(self.h2(x)))
        
        # Output layer with softmax activation
        x = F.softmax(self.output(x), dim=1)
        
        return x


model = Network()


criterion = nn.CrossEntropyLoss()
optimizer = optim.SGD(model.parameters(), lr=0.003)

epochs = 200
train_losses, test_losses = [], []
batch_size = 32

trainloader = len(X_train)/batch_size
testloader = len(X_test)/batch_size


for e in range(epochs):
    
    running_loss = 0 
    
    permutation = torch.randperm(X_train.size()[0])
    
    for i in range(0, X_train.size()[0], batch_size):
        
        optimizer.zero_grad()
        
        indices = permutation[i:i+batch_size]
        batch_x, batch_y = X_train[indices], y_train[indices]
        
        log_ps = model(batch_x)
        loss = criterion(log_ps, batch_y)
        loss.backward()
        optimizer.step()
    
        running_loss += loss.item()
        
    else:
        
        test_loss = 0
        accuracy = 0
        
        permutation = torch.randperm(X_test.size()[0])
    
        for i in range(0, X_test.size()[0], batch_size):
        
            # Turn off gradients for validation, saves memory and computations
            with torch.no_grad():
                
                model.eval()
                indices = permutation[i:i+batch_size]
                batch_x, batch_y = X_test[indices], y_test[indices]
                log_ps = model(batch_x)
                test_loss += criterion(log_ps, batch_y)
                
                ps = torch.exp(log_ps)
                top_p, top_class = ps.topk(1, dim=1)
                equals = top_class == batch_y.view(*top_class.shape)
                accuracy += torch.mean(equals.type(torch.FloatTensor))
                      
        model.train()
        
        train_losses.append(running_loss/trainloader)
        test_losses.append(test_loss/testloader)
    
        print("Epoch: {}/{}.. ".format(e+1, epochs),
              "Training Loss: {:.3f}.. ".format(running_loss/trainloader),
              "Test Loss: {:.3f}.. ".format(test_loss/testloader),
              "Test Accuracy: {:.3f}".format(accuracy/testloader))

    plt.plot(train_losses)
    plt.plot(test_losses)
