import pandas as pd
import torch
import torch.nn as nn
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
import torch.nn.functional as F
from torch import optim
import copy

def splitAndTransform(x):
 
    x = (x.split(' '))
    ans = [] 
    
    for i, data in enumerate(x):
        if not data == '':
            ans.append(float(data))
       
    return ans

def readData(filename, setHeader):
    data = pd.read_csv(filename, header = setHeader)
    return data
    
def readLabels(filename, setHeader):
    labels = pd.read_csv(filename, header = setHeader)
    return labels

def preprocessData(data):
    
    #print(data)
    
    transformedData =[]

    for i in range(len(data[0])):
        transformedData.append(splitAndTransform(data[0][i]))
    
    tensorData = torch.tensor(transformedData)
    return tensorData

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
    
def train(net, X_train, y_train, X_test, y_test):

    criterion = nn.CrossEntropyLoss()
    optimizer = optim.SGD(net.parameters(), lr=0.003)
    epochs = 50
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
            log_ps = net(batch_x)
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
                    net.eval()
                    indices = permutation[i:i+batch_size]
                    batch_x, batch_y = X_test[indices], y_test[indices]
                    log_ps = net(batch_x)
                    test_loss += criterion(log_ps, batch_y)
                    
                    ps = torch.exp(log_ps)
                    top_p, top_class = ps.topk(1, dim=1)
                    equals = top_class == batch_y.view(*top_class.shape)
                    accuracy += torch.mean(equals.type(torch.FloatTensor))

            net.train()           
            train_losses.append(running_loss/trainloader)
            test_losses.append(test_loss/testloader)
        
            if e % 24 == 0:
                print("Epoch: {}/{}.. ".format(e+1, epochs),
                      "Training Loss: {:.3f}.. ".format(running_loss/trainloader),
                      "Test Loss: {:.3f}.. ".format(test_loss/testloader),
                      "Test Accuracy: {:.3f}".format(accuracy/testloader))
  
    return net.state_dict(), train_losses, test_losses

def plotLoss(trainLoss, testLoss, clientID):
    
    title = "CLIENT " + str(clientID)

    fig = plt.figure()
    plt.plot(trainLoss, label="Train Loss")
    plt.plot(testLoss, label="Test Loss")
    plt.legend(loc="upper right")  
    fig.suptitle(title, fontsize=20)
    plt.xlabel('epochs', fontsize=18)
    plt.ylabel('loss', fontsize=16)    
    plt.show()
    plt.clf()
    
def plotFedTestLoss(testLosses, clients):
    
    
    fig = plt.figure()
    
    for i in range(len(testLosses)):

        title = "CLIENT " + str(clients[i])
        plt.plot(testLosses[i], label=title)
        plt.legend(loc="upper right")  
        #fig.suptitle(title, fontsize=20)
        plt.xlabel('iterations', fontsize=18)
        plt.ylabel('loss', fontsize=16)    
    
    plt.show()
    plt.clf()
    

def FedAvg(weights):
    
    w_avg = copy.deepcopy(weights[0])
    
    for k in w_avg.keys():
        
        for i in range(1, len(weights)):
            w_avg[k] += weights[i][k]
            
        w_avg[k] = torch.div(w_avg[k], len(weights))
    
    return w_avg

def fedTrain(model, X_train, y_train, X_test, y_test, numOfClients, iters):
    
    w_glob = model.state_dict()
    
    m = max(int(0.5 * numOfClients), 1)
    idxs_users = np.random.choice(range(numOfClients), m, replace=False)
    
    testLosses = []
    
    for i in range(len(idxs_users)):
        testLosses.append([])

    for iteration in range(iters):
        
        w_locals = []
                        
        print("\n************ ITERATION ", iteration+1, " - FED AVG *******************")
        
        for i, idx in enumerate(idxs_users):
            
           print("\nCLIENT:", idx , "\n")
           net=copy.deepcopy(model)
           
           w, trainLoss, testLoss = train(net, X_train[idx], y_train[idx], X_test, y_test)
           w_locals.append(copy.deepcopy(w))

           testLosses[i].append(testLoss[-1])
           
           plotLoss(trainLoss, testLoss, idx)
           
           #loss_locals.append(copy.deepcopy(loss))
           # update global weights
        
        w_glob = FedAvg(w_locals)
        
        # copy weight to net_glob
        model.load_state_dict(w_glob)

    plotFedTestLoss(testLosses, idxs_users)        
    return model
    
def splitRandomAndTrain(model, X_train, y_train, X_test, y_test, iters):
    
    X_split_dataset = torch.split(X_train, 1000)
    y_split_dataset = torch.split(y_train, 1000)
    
    #print(X_split_dataset)
    
    model = fedTrain(model, X_split_dataset, y_split_dataset, X_test, y_test, \
                     len(X_split_dataset), iters)
    
    return model

def splitByLabels(model, trainData, X_train, y_train, X_test, y_test, iters):
    
   
    X_split_dataset = []
    y_split_dataset = []
    
    
    groups = trainData.groupby('labels').apply(lambda x: x.index.tolist())
    
    for i in range(1,7):
        indices = torch.tensor(groups[i])
        
        x = torch.index_select(X_train, 0, indices)
        X_split_dataset.append(x)
        
        y = torch.index_select(y_train, 0, indices)
        y_split_dataset.append(y)
    
    model = fedTrain(model, X_split_dataset, y_split_dataset, X_test, y_test, \
                     len(X_split_dataset), iters)
    
    return model

def main():
    
    trainData = readData('UCI HAR Dataset/UCI HAR Dataset/train/X_train.txt', None)
    trainData['labels'] = readLabels('UCI HAR Dataset/UCI HAR Dataset/train/y_train.txt', None)
    
    X_train = preprocessData(trainData)
    y_train = torch.tensor(trainData['labels'].values).flatten()
    
    testData = readData('UCI HAR Dataset/UCI HAR Dataset/test/X_test.txt', None)
    testData['labels'] = readLabels('UCI HAR Dataset/UCI HAR Dataset/test/y_test.txt', None)

    X_test = preprocessData(testData)
    y_test = torch.tensor(testData['labels'].values).flatten()

    model = Network()
    
    #splitRandomAndTrain(model, X_train, y_train, X_test, y_test, 6)
    splitByLabels(model, trainData, X_train, y_train, X_test, y_test, 6)
    
    

if __name__ == "__main__":
    main()

#dict_of_regions = {k: v for k, v in xHarData.groupby('labels')}
#print(dict_of_regions)

'''
for idx in idxs_users:
   print("USER:", idx)
   net=copy.deepcopy(model)
   w = train(net, X_split_dataset[idx], y_split_dataset[idx])
   plt.show()
   w_locals.append(copy.deepcopy(w))
''' 

